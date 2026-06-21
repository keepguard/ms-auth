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

    @Value("${cache.redis.prefix.token:tokenlogin:}")
    private String tokenPrefix;

    @Value("${cache.redis.prefix.reset-token:resetpassword:}")
    private String resetTokenPrefix;

    @Value("${cache.redis.ttl.token:3600}")
    private long tokenTtlSeconds;

    @Value("${cache.redis.ttl.reset-token:900}")
    private long resetTokenTtlSeconds;

    @Override
    @CircuitBreaker(name = "redisCache")
    public void saveToken(String codeUser, String token, long ttlMillis) {
        try {
            String key = tokenPrefix + codeUser + ":" + token;
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
            String key = tokenPrefix + codeUser + ":" + token;
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
            Set<String> keys = redisTemplate.keys(tokenPrefix + codeUser + ":*");
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
            String key = tokenPrefix + codeUser + ":" + token;
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
            
            log.info("Token de reset gerado e salvo com sucesso | codeUser={} | messageType={} | templateType={} | ttl={}s", 
                codeUser, messageType, templateType, resetTokenTtlSeconds);
            
            return token;
        } catch (Exception e) {
            log.error("Erro ao gerar e salvar token de reset | codeUser={} | messageType={} | templateType={} | erro={}", 
                codeUser, messageType, templateType, e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar token de reset", e);
        }
    }

    /**
     * Constrói a chave composta para o token de reset no Redis.
     * Formato: resetpassword:codeUser:messageType:templateType
     */
    private String buildResetTokenKey(String codeUser, String messageType, String templateType) {
        return String.format("%s%s:%s:%s", resetTokenPrefix, codeUser, messageType, templateType);
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
