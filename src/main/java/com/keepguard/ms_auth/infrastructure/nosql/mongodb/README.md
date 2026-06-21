# MongoDB - Preparado para Integração Futura

## 📋 Estrutura Criada

Esta estrutura está **pronta** para receber a integração com MongoDB quando necessário.

### Arquivos Base:
- ✅ `MongoDBAdapter.java` - Operações CRUD (preparado)
- ✅ Port: `application/port/out/nosql/NoSQLRepositoryPort.java`

---

## 🚀 Como Habilitar MongoDB

### Passo 1: Adicionar Dependência

```xml
<!-- Spring Data MongoDB -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### Passo 2: Configurar application.yml

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/keepguard_auth}
      database: keepguard_auth

# Adicionar instância de resiliência para MongoDB
resilience4j:
  circuitbreaker:
    instances:
      mongodbOperation:
        failureRateThreshold: 60
        waitDurationInOpenState: 30s
  retry:
    instances:
      mongodbOperation:
        maxAttempts: 2
        waitDuration: 1s
  timelimiter:
    instances:
      mongodbQuery:
        timeoutDuration: 10s
```

### Passo 3: Criar Entidade MongoDB

```java
@Document(collection = "audit_logs")
@Getter
@Setter
@Builder
public class AuditLogDocument {
    
    @Id
    private String id;
    
    @Field("user_id")
    private UUID userId;
    
    @Field("action")
    private String action;
    
    @Field("timestamp")
    private LocalDateTime timestamp;
    
    @Field("details")
    private Map<String, Object> details;
    
    @Field("ip_address")
    private String ipAddress;
}
```

### Passo 4: Implementar Adapter com Resiliência

```java
@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements NoSQLRepositoryPort<AuditLogDocument, String> {

    private final MongoTemplate mongoTemplate;

    @CircuitBreaker(name = "mongodbOperation", fallbackMethod = "saveFallback")
    @Retry(name = "mongodbOperation")
    @TimeLimiter(name = "mongodbQuery")
    @Override
    public AuditLogDocument save(AuditLogDocument entity) {
        return mongoTemplate.save(entity);
    }

    private AuditLogDocument saveFallback(AuditLogDocument entity, Exception ex) {
        log.error("⚠️ FALLBACK: MongoDB indisponível. Audit log não salvo");
        // Poderia salvar em PostgreSQL como fallback
        return null;
    }

    @CircuitBreaker(name = "mongodbOperation", fallbackMethod = "findByIdFallback")
    @Retry(name = "mongodbOperation")
    @TimeLimiter(name = "mongodbQuery")
    @Override
    public Optional<AuditLogDocument> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, AuditLogDocument.class));
    }

    private Optional<AuditLogDocument> findByIdFallback(String id, Exception ex) {
        log.error("⚠️ FALLBACK: MongoDB indisponível. Retornando empty");
        return Optional.empty();
    }
}
```

---

## 📊 Casos de Uso

### 1. Audit Trail (Logs de Auditoria)

```java
// Registrar login do usuário
AuditLogDocument log = AuditLogDocument.builder()
    .userId(userId)
    .action("LOGIN")
    .timestamp(LocalDateTime.now())
    .ipAddress(request.getRemoteAddr())
    .details(Map.of(
        "userAgent", userAgent,
        "success", true
    ))
    .build();

auditLogRepository.save(log);
```

### 2. Event Sourcing (Histórico de Eventos)

```java
// Armazenar histórico completo de mudanças
UserEventDocument event = UserEventDocument.builder()
    .userId(userId)
    .eventType("PASSWORD_CHANGED")
    .eventData(eventData)
    .version(1)
    .timestamp(LocalDateTime.now())
    .build();

eventRepository.save(event);
```

### 3. Read Model para CQRS

```java
// Modelo otimizado para leitura/análises
UserStatsDocument stats = UserStatsDocument.builder()
    .userId(userId)
    .loginCount(42)
    .lastLogin(LocalDateTime.now())
    .failedLoginAttempts(0)
    .build();

statsRepository.save(stats);
```

---

## ✅ Benefícios da Estrutura Preparada

- ✅ Arquitetura Hexagonal mantida (Ports & Adapters)
- ✅ Resiliência já planejada (Circuit Breaker, Retry, Timeout)
- ✅ Fácil habilitação quando necessário
- ✅ Zero impacto no código atual
- ✅ Separação clara de responsabilidades
- ✅ Documentação completa

---

## 🎯 Próximos Passos (quando habilitar)

1. [ ] Adicionar dependência spring-boot-starter-data-mongodb
2. [ ] Configurar connection string no application.yml
3. [ ] Criar entidades MongoDB (@Document)
4. [ ] Implementar repositories
5. [ ] Aplicar Circuit Breaker, Retry e Timeout
6. [ ] Criar testes com Testcontainers (MongoDB)
