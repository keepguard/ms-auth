package com.keepguard.ms_auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.port.out.cache.RefreshTokenCachePort;
import com.keepguard.ms_auth.domain.entity.session.RefreshToken;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCacheService implements RefreshTokenCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_PREFIX = "refreshtoken:";
    private static final String FAMILY_REVOKED_PREFIX = "refreshtoken:family:revoked:";
    private static final String SID_TOKENS_PREFIX = "refreshtoken:sid:";
    private static final String SESSION_REVOKED_PREFIX = "session:revoked:";

    @Override
    @CircuitBreaker(name = "redisCache")
    public void save(RefreshToken refreshToken, long slidingTtlSeconds) {
        try {
            String key = tokenKey(refreshToken.getTokenHash());
            String json = objectMapper.writeValueAsString(refreshToken);
            redisTemplate.opsForValue().set(key, json, slidingTtlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForSet().add(sidTokensKey(refreshToken.getSid()), refreshToken.getTokenHash());
            redisTemplate.expire(sidTokensKey(refreshToken.getSid()), slidingTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao salvar refresh token | sid={} | erro={}", refreshToken.getSid(), e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache", fallbackMethod = "findByHashFallback")
    public Optional<RefreshToken> findByHash(String tokenHash) {
        try {
            String json = redisTemplate.opsForValue().get(tokenKey(tokenHash));
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, RefreshToken.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<RefreshToken> findByHashFallback(String tokenHash, Exception ex) {
        log.warn("FALLBACK: Redis indisponível para busca de refresh token | erro={}", ex.getClass().getSimpleName());
        return Optional.empty();
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void markReplaced(String oldTokenHash, String newTokenHash, long graceTtlSeconds) {
        try {
            String key = tokenKey(oldTokenHash);
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return;
            }
            RefreshToken old = objectMapper.readValue(json, RefreshToken.class);
            old.setReplacedByHash(newTokenHash);
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(old), graceTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao marcar refresh token como substituído | erro={}", e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void revokeFamily(String familyId) {
        try {
            redisTemplate.opsForValue().set(FAMILY_REVOKED_PREFIX + familyId, "1");
            log.warn("Família de refresh token revogada | familyId={}", familyId);
        } catch (Exception e) {
            log.warn("Falha ao revogar família de refresh token | familyId={} | erro={}", familyId, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache", fallbackMethod = "isFamilyRevokedFallback")
    public boolean isFamilyRevoked(String familyId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(FAMILY_REVOKED_PREFIX + familyId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isFamilyRevokedFallback(String familyId, Exception ex) {
        log.warn("FALLBACK: Redis indisponível para checagem de família revogada | familyId={} | erro={}",
                familyId, ex.getClass().getSimpleName());
        return true;
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeBySid(String sid) {
        try {
            String sidKey = sidTokensKey(sid);
            Set<String> hashes = redisTemplate.opsForSet().members(sidKey);
            if (hashes != null && !hashes.isEmpty()) {
                for (String hash : hashes) {
                    redisTemplate.delete(tokenKey(hash));
                }
            }
            redisTemplate.delete(sidKey);
        } catch (Exception e) {
            log.warn("Falha ao remover refresh tokens por sid | sid={} | erro={}", sid, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void markSessionRevoked(String sid, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(SESSION_REVOKED_PREFIX + sid, "1", ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao marcar sessão como revogada | sid={} | erro={}", sid, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache", fallbackMethod = "isSessionRevokedFallback")
    public boolean isSessionRevoked(String sid) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_REVOKED_PREFIX + sid));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSessionRevokedFallback(String sid, Exception ex) {
        log.warn("FALLBACK: Redis indisponível para checagem de sessão revogada | sid={} | erro={}",
                sid, ex.getClass().getSimpleName());
        return false;
    }

    private String tokenKey(String tokenHash) {
        return TOKEN_PREFIX + tokenHash;
    }

    private String sidTokensKey(String sid) {
        return SID_TOKENS_PREFIX + sid;
    }
}
