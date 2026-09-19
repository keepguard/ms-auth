# ms-auth - System Design

## 1. Propósito e Domínio
- **Responsabilidade Principal:** Microsserviço de autenticação e autorização da plataforma KeepGuard: emite/valida JWT (usuário e client OAuth de serviço), gerencia credenciais de usuário, roles/authorities, sessões por dispositivo e ciclo de vida de conta (block/unlock/delete). Também provisiona papéis por empresa e participa da eliminação de titular (LGPD Art. 18) via mensageria.
- **Domínio/Subdomínio:** IAM (Identity & Access Management) — AuthN/AuthZ, OAuth client credentials, session/device trust, RBAC multi-empresa.

## 2. Tech Stack Local
- **Linguagem & Framework:** Java 25 / Spring Boot 3.5.3 (Spring Cloud 2025.0.3); Web, Security (OAuth2 Resource Server), Data JPA, AMQP, OpenFeign, AOP, Actuator, Springdoc OpenAPI 2.3.0, Resilience4j 2.2.0, JJWT 0.12.5, Lombok; lib interna `lib-common` 1.0.37-SNAPSHOT; virtual threads habilitadas (`spring.threads.virtual.enabled`).
- **Persistência e Cache:** PostgreSQL (`keepguard_api_db`, schema Hibernate `ms_auth`, HikariCP); Redis (standalone em local; cluster 6 nós em dev/prod) para cache de user/roles/role, tokens de login/reset e sessões; MongoDB **não ativo** (adapter stub preparado em `infrastructure/nosql/mongodb`).
- **Mensageria:** RabbitMQ — (1) consome `user.erasure.requested` na fila `ms.auth.user-erasure` (exchange topic `keepguard-events-exchange`); (2) publica auditoria via `lib-common` `@LogOperation` no exchange configurável `keepguard.audit.exchange` (routing-key `audit.event`, `source-service: ms-auth`). Porta `EventPublisherPort` existe, sem implementação concreta neste serviço.

## 3. Arquitetura Interna
- **Padrão Utilizado:** Hexagonal (Ports & Adapters) com camadas DDD leves: `adapters/in` (REST), `adapters/out` (Feign), `application` (use cases + ports in/out), `domain` (entidades/enums/specifications), `infrastructure` (JPA, Redis, security, messaging, filters, resilience).
- **Módulos Principais:**
  - **Auth:** login/register-login, refresh, logout, validate, change/reset password, generate-reset-token (`AuthPort` → `AuthUseCaseService` / `AuthCommandService`).
  - **User:** create (user/admin/manager), soft/hard delete, block/unlock, validate-email, roles, busca (`UserPort`).
  - **Role / Authority / CompanyRole:** CRUD de roles e authorities; provisionamento `POST /api/v1/companies/{companyId}/roles/provision`; roles de sistema (`ROLE_ADMIN|MANAGER|USER|SYSTEM`) e service roles OAuth (`ROLE_SERVICE_*`).
  - **OAuth Client:** CRUD de clients por `companyId`, emissão de service token, runtime secret.
  - **Session/Device:** challenge MFA de dispositivo, sessões Redis, blacklist de devices, quick-revoke, escopos self/tenant/admin.
  - **Seeders:** `AuthoritySeeder`, `RoleSeeder`, `ServiceRoleSeeder` na subida.
  - **Security/Infra:** `JwtService`, `LoginAttemptService`, filtros `CorrelationIdFilter` / `JwtAuditMdcFilter`, adapters JPA e caches Redis.

## 4. Superfície de Contato (I/O)
- **Endpoints Expostos Principais:**
  - `POST /api/v1/auth/login|register-login|refresh|logout|validate|change-password|reset-password|generate-reset-token`
  - `POST /api/v1/auth/oauth/token`; `GET /api/v1/auth/oauth/runtime/secret`; CRUD `/api/v1/auth/oauth/clients`
  - `POST /api/v1/auth/device/challenge/send|verify`; `GET|POST /api/v1/auth/device/quick-revoke`
  - CRUD/lifecycle `/api/v1/users/**` (create, create-admin, create-manager, delete, hard-delete, block, unlock, roles, search, status-history)
  - Sessões/blacklist: `/api/v1/users/me/sessions/**`, `/api/v1/users/{userId}/sessions/**`, `/api/v1/admin/devices/blacklist`, `/api/v1/devices/blacklist`, `/api/v1/sessions`
  - `/api/v1/roles/**`, `/api/v1/authorities/**`, `POST /api/v1/companies/{companyId}/roles/provision`
  - Health: `/api/v1/health`, `/api/v1/helper/health`; Actuator `health/info/prometheus/metrics`
  - Portas de processo: **8581** (local/base), **8382** (dev), **8081** (prod)
- **Dependências Externas:**
  - **ms-communication** (Feign): `POST /api/v1/messages/send` (e-mail/SMS de reset/challenge; header `X-Company-Id`)
  - **ms-user** (Feign): `GET /internal/v1/users/code/{codeUser}` (dados auxiliares, ex. telefone MFA)
  - **ms-company** (Feign): `GET /api/v1/companies/{id}` e `/api/v1/companies/x-tenant-id/{tenantId}`
  - **GeoIP APIs:** geojs.io (primário) e ipwho.is (fallback) via Feign + `IpWhoIsGeoLocationAdapter`
  - **PostgreSQL**, **Redis**, **RabbitMQ** (eventos + audit); OAuth2 JWT issuer/JWKS configuráveis (`SECURITY_ISSUER_URI` / `SECURITY_JWKS_URI`)

## 5. Invariantes Locais e Observações
- **Multi-tenancy por empresa:** usuários, roles, OAuth clients e operações sensíveis são escopados por `companyId` / `tenantId` (claims JWT `tenant_id` / `company_id`); unicidade de username/email/`idUserExternal` é por empresa.
- **Estados de usuário:** `ACTIVE | BLOCKED | DELETED`; soft-delete marca `DELETED`; hard-delete e consumer LGPD removem credenciais e revogam todas as sessões.
- **Hierarquia de ciclo de vida:** `AccountLifecycleRank` (USER < MANAGER < ADMIN < SYSTEM) + authorities distintas (`user:*` vs `manager:*`) para block/unblock/delete; rate limiters `createManager` e `accountLifecycle`.
- **Roles reservadas:** `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_USER`, `ROLE_SYSTEM`; criação de usuário exige company roles default habilitadas (`CompanyDefaultRolesNotConfiguredException` se ausentes).
- **Credenciais:** senha com BCrypt; histórico de senhas (`PasswordHistory`); JWT HS (secret + expiration 1h) para usuário e TTL configurável para service tokens (min 15m / default 8h / max 24h); secrets OAuth com crypto dedicada (`OAuthClientSecretCrypto` + `AUTH_CLIENT_SECRET_BASE`).
- **Proteção de login:** rate limit Resilience4j `loginAttempt` (5/min) + `LoginAttemptService` (max 5 tentativas, lockout 15 min); tokens de reset com max 3 tentativas e cooldown 60s; cache Redis de reset-token TTL 10 min.
- **Device trust:** challenge de dispositivo, blacklist (self/tenant/admin), quick-revoke; geolocalização enriquecida no fluxo de sessão.
- **Security surface:** grande parte de `/api/v1/**` está `permitAll` no `SecurityConfig` (auth forte aplicada seletivamente a OAuth clients, block/unlock/delete, me/sessions/blacklist/admin); method security (`@EnableMethodSecurity`) e authorities de domínio complementam.
- **Schema/DDL:** `ddl-auto: update` em local/dev e `validate` em prod; schema fixo `ms_auth`.
- **Observabilidade:** Micrometer/Prometheus, health de circuit breakers/rate limiters, correlation-id em Feign/HTTP, auditoria MDC + RabbitMQ; MongoDB e Logstash mencionados mas desabilitados/preparados.
