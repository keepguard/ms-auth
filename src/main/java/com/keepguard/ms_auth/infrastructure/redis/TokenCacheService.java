package com.keepguard.ms_auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.lib_common.utils.CodeGeneratorUtils;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCacheService implements TokenCachePort {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.redis.prefix.token}")
    private String tokenPrefix;

    @Value("${cache.redis.prefix.reset-token}")
    private String resetTokenPrefix;

    @Value("${cache.redis.ttl.token}")
    private long tokenTtlSeconds;

    @Value("${cache.redis.ttl.reset-token}")
    private long resetTokenTtlSeconds;

    @Value("${security.rate-limiting.reset-token-max-attempts}")
    private int resetTokenMaxAttempts;

    @Value("${security.rate-limiting.reset-token-cooldown-seconds}")
    private int resetTokenCooldownSeconds;

    private static final String RESET_TOKEN_COOLDOWN_PREFIX = "reset_token_cooldown:";
    private static final String RESET_TOKEN_ATTEMPTS_PREFIX = "reset_token_attempts:";

    @Override
    @CircuitBreaker(name = "redisCache")
    public void saveToken(String codeUser, String token, long ttlMillis) {
        try {
            String key = loginTokenKey(codeUser, token);
            Instant now = Instant.now();
            Instant expiresAt = now.plusMillis(ttlMillis);
            
            String value = objectMapper.writeValueAsString(
                new TokenInfo(codeUser, token, now.toString(), expiresAt.toString())
            );
            redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Falha ao salvar token | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache", fallbackMethod = "isTokenValidFallback")
    @Retry(name = "redisCache")
    public boolean isTokenValid(String codeUser, String token) {
        try {
            String key = loginTokenKey(codeUser, token);
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isTokenValidFallback(String codeUser, String token, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel para validacao de token | codeUser={} | erro={}", 
            codeUser, ex.getClass().getSimpleName());
        return false;
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeAllTokens(String codeUser) {
        try {
            Set<String> keys = redisTemplate.keys(tokenBasePrefix() + ":" + normalize(codeUser) + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Tokens removidos | codeUser={} | quantidade={}", codeUser, keys.size());
            }
        } catch (Exception e) {
            log.warn("Falha ao remover tokens | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeToken(String codeUser, String token) {
        try {
            String key = loginTokenKey(codeUser, token);
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover token | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void saveToken(String codeUser, String messageType, String templateType, String token, long ttlMillis) {
        try {
            String key = buildResetTokenKey(codeUser, messageType, templateType);
            Instant now = Instant.now();
            Instant expiresAt = now.plusMillis(ttlMillis);
            
            String value = objectMapper.writeValueAsString(
                new ResetTokenInfo(token, now.toString(), expiresAt.toString())
            );
            redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
            log.debug("Token de reset salvo | key={}", key);
        } catch (Exception e) {
            log.warn("Falha ao salvar token de reset | codeUser={} | messageType={} | templateType={} | erro={}", 
                codeUser, messageType, templateType, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache", fallbackMethod = "isResetTokenValidFallback")
    @Retry(name = "redisCache")
    public boolean isResetTokenValid(String codeUser, String messageType, String templateType, String token) {
        try {
            String key = buildResetTokenKey(codeUser, messageType, templateType);
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                log.debug("Token de reset não encontrado | key={}", key);
                return false;
            }
            
            ResetTokenInfo info = objectMapper.readValue(value, ResetTokenInfo.class);
            boolean isValid = info.token.equals(token);
            log.debug("Validação de token de reset | key={} | isValid={}", key, isValid);
            return isValid;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isResetTokenValidFallback(String codeUser, String messageType, String templateType, String token, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel para validacao de token de reset | codeUser={} | messageType={} | templateType={} | erro={}", 
            codeUser, messageType, templateType, ex.getClass().getSimpleName());
        return false;
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeResetToken(String codeUser, String messageType, String templateType) {
        try {
            String key = buildResetTokenKey(codeUser, messageType, templateType);
            redisTemplate.delete(key);
            log.debug("Token de reset removido | key={}", key);
        } catch (Exception e) {
            log.warn("Falha ao remover token de reset | codeUser={} | messageType={} | templateType={} | erro={}", 
                codeUser, messageType, templateType, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public String generateAndSaveResetToken(String codeUser, String messageType, String templateType) {
        try {
            // Gera token de 6 dígitos numéricos
            String token = CodeGeneratorUtils.generateSixDigitCode();
            
            // Calcula TTL em milissegundos
            long ttlMillis = resetTokenTtlSeconds * 1000;
            
            // Salva o token no cache
            saveToken(codeUser, messageType, templateType, token, ttlMillis);

            // Define o cooldown para novas gerações
            String cooldownKey = buildCooldownKey(codeUser);
            redisTemplate.opsForValue().set(cooldownKey, Instant.now().toString(), resetTokenCooldownSeconds, TimeUnit.SECONDS);

            // Limpa tentativas anteriores
            clearResetTokenAttempts(codeUser, messageType, templateType);
            
            log.info("Token de reset gerado e salvo com sucesso | codeUser={} | messageType={} | templateType={} | ttl={}s | cooldown={}s", 
                codeUser, messageType, templateType, resetTokenTtlSeconds, resetTokenCooldownSeconds);
            
            return token;
        } catch (Exception e) {
            log.error("Erro ao gerar e salvar token de reset | codeUser={} | messageType={} | templateType={} | erro={}", 
                codeUser, messageType, templateType, e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar token de reset", e);
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public boolean isResetTokenCooldownActive(String codeUser) {
        try {
            String cooldownKey = buildCooldownKey(codeUser);
            return Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey));
        } catch (Exception e) {
            log.warn("Falha ao verificar cooldown | codeUser={} | erro={}", codeUser, e.getMessage());
            return false;
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public long getResetTokenCooldownRemaining(String codeUser) {
        try {
            String cooldownKey = buildCooldownKey(codeUser);
            Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.warn("Falha ao obter tempo restante de cooldown | codeUser={} | erro={}", codeUser, e.getMessage());
            return 0;
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public long recordResetTokenFailedAttempt(String codeUser, String messageType, String templateType) {
        try {
            String attemptsKey = buildResetTokenAttemptsKey(codeUser, messageType, templateType);
            Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
            if (attempts != null && attempts == 1) {
                // TTL sincronizado com a validade do reset token
                redisTemplate.expire(attemptsKey, resetTokenTtlSeconds, TimeUnit.SECONDS);
            }
            log.warn("Tentativa incorreta de token de reset registrada | codeUser={} | messageType={} | templateType={} | tentativa={}/{}", 
                codeUser, messageType, templateType, attempts, resetTokenMaxAttempts);
            
            if (attempts != null && attempts >= resetTokenMaxAttempts) {
                log.warn("Limite de tentativas de token de reset excedido. Invalidando token | codeUser={}", codeUser);
                removeResetToken(codeUser, messageType, templateType);
            }
            return attempts != null ? attempts : 0;
        } catch (Exception e) {
            log.warn("Falha ao registrar tentativa incorreta de token | codeUser={} | erro={}", codeUser, e.getMessage());
            return 0;
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public boolean isResetTokenAttemptsExceeded(String codeUser, String messageType, String templateType) {
        try {
            String attemptsKey = buildResetTokenAttemptsKey(codeUser, messageType, templateType);
            String value = redisTemplate.opsForValue().get(attemptsKey);
            if (value == null) {
                return false;
            }
            return Integer.parseInt(value) >= resetTokenMaxAttempts;
        } catch (Exception e) {
            log.warn("Falha ao verificar limite de tentativas | codeUser={} | erro={}", codeUser, e.getMessage());
            return false;
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void clearResetTokenAttempts(String codeUser, String messageType, String templateType) {
        try {
            String attemptsKey = buildResetTokenAttemptsKey(codeUser, messageType, templateType);
            redisTemplate.delete(attemptsKey);
        } catch (Exception e) {
            log.warn("Falha ao limpar tentativas de reset token | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    /**
     * Constrói a chave composta para o token de reset no Redis.
     * Formato: resetpassword:codeUser:messageType:templateType
     */
    private String buildResetTokenKey(String codeUser, String messageType, String templateType) {
        return String.format("%s:%s:%s:%s", resetTokenBasePrefix(), normalize(codeUser), messageType, templateType);
    }

    /**
     * Constrói a chave para contagem de tentativas do token de reset no Redis.
     * Formato: reset_token_attempts:codeUser:messageType:templateType
     */
    private String buildResetTokenAttemptsKey(String codeUser, String messageType, String templateType) {
        return String.format("%s%s:%s:%s", RESET_TOKEN_ATTEMPTS_PREFIX, normalize(codeUser), messageType, templateType);
    }

    private String loginTokenKey(String codeUser, String token) {
        return tokenBasePrefix() + ":" + normalize(codeUser) + ":" + token;
    }

    private String buildCooldownKey(String codeUser) {
        return RESET_TOKEN_COOLDOWN_PREFIX + normalize(codeUser);
    }

    private String tokenBasePrefix() {
        if (tokenPrefix == null || tokenPrefix.isBlank()) {
            return "tokenlogin";
        }
        return tokenPrefix.replaceAll(":+$", "");
    }

    private String resetTokenBasePrefix() {
        if (resetTokenPrefix == null || resetTokenPrefix.isBlank()) {
            return "resetpassword";
        }
        return resetTokenPrefix.replaceAll(":+$", "");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public record TokenInfo(
        String codeUser,
        String token,
        String createdAt,
        String expiresAt
    ) {}

    public record ResetTokenInfo(
        String token,
        String createdAt,
        String expiresAt
    ) {}
}
