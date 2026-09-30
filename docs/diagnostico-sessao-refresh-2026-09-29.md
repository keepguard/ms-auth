# Diagnóstico: sessão do backoffice expirando (2026-09-29)

## Veredito

É **lógica (desenho)**, não só tempo. O tempo de 1h só deixa o problema visível.
**Não existe refresh token de verdade**: o "refresh token" é o próprio access token JWT.

## Como funciona hoje

1. Login (`ms-auth` `AuthCommandService`) emite **um** JWT de 1h (`security.jwt.expiration: 3600000`)
   e grava em Redis `tokenlogin:<user>:<token>` com TTL 1h.
2. `bff-auth` devolve esse JWT no corpo e grava o mesmo JWT no cookie `keepguard_refresh_token`
   (cookie de 7 dias, conteúdo que vence em 1h).
3. Front (`tokenStore.ts`) guarda em `sessionStorage` por aba; `refreshToken = accessToken`
   (`AuthContext.login`: `data.refreshToken || token`).
4. Refresh (`AuthCommandService.refreshToken`): exige JWT **não expirado** (`parseSignedClaims`
   lança `ExpiredJwtException`) **e** presente no Redis; emite JWT novo e **apaga o antigo**.
5. `bff-core` (`jwt_middleware.go`) rejeita qualquer token que não esteja no Redis (`TOKEN_REVOKED`).

## As 4 causas de "fui jogado para fora"

| # | Cenário | Onde | Tipo |
|---|---|---|---|
| 1 | **Várias janelas**: aba A renova → token antigo apagado do Redis → aba B, na próxima troca de página, leva 401 `TOKEN_REVOKED` do bff-core → tenta refresh mandando **o token velho no corpo** (o bff-auth prioriza corpo sobre cookie) → `TOKEN_REVOKED` → `clearTokens` → login | ms-auth rotação + `bff-auth RefreshHandler` + `tokenStore.ensureFreshToken` | lógica |
| 2 | **Aba parada > 1h**: o timer não renova se não houve mouse/teclado em 5 min (`IDLE_THRESHOLD_MS`). Aba em segundo plano nunca tem atividade → JWT vence → refresh impossível (JWT vencido não passa) | `tokenStore.runScheduledRefresh` + ms-auth | lógica |
| 3 | **Notebook dormiu / aba congelada pelo Chrome**: timer atrasa além de 1h → mesmo efeito do #2 | desenho de 1 token só | lógica |
| 4 | **Qualquer 401 derruba a sessão**: se o retry após refresh ainda der 401 (ex.: serviço que responde 401 por motivo que não é sessão), `customFetch` chama `clearTokens` | `api.ts` linhas 171–182 | técnico |

## Achados de segurança (independentes do bug)

- **Segredo do JWT commitado**: `ms-auth/src/main/resources/application-prod.yml` tem
  `security.jwt.secret` em texto; `application.yml` tem o mesmo valor como default. Quem tem o
  repositório consegue forjar token de qualquer usuário. **Trocar o segredo e mover para Secret.**
- Token de longa duração acessível a JavaScript (`sessionStorage` + corpo do refresh): um XSS
  leva a sessão inteira.
- Cookie com `SameSite=None` sem necessidade: front e API estão no mesmo site
  (`*.keepguard.com.br`), então `Lax`/`Strict` funciona.

## Solução proposta (padrão "Google")

**Access token curto em memória + refresh token opaco em cookie HttpOnly, com rotação tolerante
a várias abas.**

### Backend

| Peça | Mudança |
|---|---|
| ms-auth login | Emitir par: access JWT **15 min** + refresh **opaco** (256 bits aleatórios), guardado só o **hash** em Redis por sessão/dispositivo (`sid`). |
| Validade | Refresh com TTL **deslizante 30 dias** (cada uso renova) e **teto absoluto 90 dias**; depois disso, login (e MFA se o dispositivo não for confiável). |
| Rotação | Cada refresh gera refresh novo. O antigo fica válido por **~60 s de carência** devolvendo o mesmo par novo (cobre abas renovando juntas). Reuso depois da carência = roubo → **revoga a família inteira** da sessão. |
| Revogação | JWT ganha claim `sid`. `bff-core` passa a checar **"sessão `sid` revogada?"** em vez de "este token exato existe?". Renovar não invalida o access token das outras abas; logout/revogação derruba tudo na hora. |
| bff-auth | Refresh lê **só o cookie** (ignora corpo). Cookie `HttpOnly; Secure; SameSite=Lax; Path=/api/v1/auth`. |
| Segredo | Trocar e tirar do repo; idealmente RS256 (BFFs só com chave pública). |

### Front (`tokenStore.ts`, `api.ts`, `AuthContext.tsx`)

| Peça | Mudança |
|---|---|
| Armazenamento | Access token **só em memória**. Nada de token em `sessionStorage`/`localStorage`. F5 ou aba nova = refresh silencioso pelo cookie. |
| Várias abas | `navigator.locks.request('kg-refresh')` garante **uma** renovação por vez entre abas; o resultado vai para as outras por `BroadcastChannel('kg-auth')`. Logout também é propagado. |
| Timer | Tirar a regra de ociosidade de 5 min. Renovar ~2 min antes do `exp`, e também em `visibilitychange`/`focus`/`online` se o token estiver vencido ou perto. |
| 401 | Só encerra a sessão quando o **refresh** responder 401. 401 de outro endpoint após o retry vira erro da tela, não logout. |

### Resultado esperado

- Aba parada o dia inteiro, notebook dormindo, 10 janelas abertas: sessão continua (até 30 dias sem uso).
- Troca de página em qualquer aba não derruba mais ninguém.
- Roubo de refresh token é detectado pelo reuso; logout e "encerrar outras sessões" continuam imediatos.

## Opção rápida (curativo, ~0,5 dia) — não recomendada como fim

1. bff-auth: refresh prioriza **cookie** sobre corpo; front manda corpo vazio.
2. ms-auth: não apagar o token antigo no refresh (deixar vencer sozinho).
3. Front: tirar a regra de ociosidade; renovar em `visibilitychange`.

Resolve o caso das várias janelas (#1) e melhora o #2, mas **qualquer ausência > 1h
(notebook fechado) continua deslogando**. Não atende "sempre ativa".

## Fases sugeridas

| Fase | Escopo | Deploy |
|---|---|---|
| 0 | Trocar segredo do JWT e mover para Secret | ms-auth, bff-core, bff-auth, bff-invest (todos que validam JWT) — derruba todas as sessões uma vez |
| 1 | ms-auth: refresh opaco, `sid`, rotação com carência, revogação por sessão; login devolve os dois (compatível: `token` continua vindo) | ms-auth |
| 2 | bff-auth cookie só-refresh + bff-core checando `sid` | bff-auth, bff-core |
| 3 | Front: memória + Web Locks + BroadcastChannel + fim da ociosidade | front-keepguard-core |

Fica de fora de propósito: "lembrar de mim" opcional, gestão de sessões na UI (já existe), achadinhos-front (verificar depois se usa o mesmo fluxo).
