package com.keepguard.ms_auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.port.out.cache.UserCachePort;
import com.keepguard.ms_auth.domain.entity.user.User;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService implements UserCachePort {

    private static final String CONTEXT = "auth";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.redis.ttl.user}")
    private long userTtlSeconds;

    @Value("${cache.redis.ttl.user-roles}")
    private long userRolesTtlSeconds;

    @Value("${cache.redis.prefix.user}")
    private String userCachePrefix;

    @Value("${cache.redis.prefix.user-roles}")
    private String userRolesCachePrefix;

    @CircuitBreaker(name = "redisCache")
    public void cacheUserByUsername(UUID companyId, String username, UserGetByUsernameView user) {
        try {
            String key = scopedKey("username", companyId, username);
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, userTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear usuario | username={} | erro={}", username, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "scopedCacheFallback")
    @Retry(name = "redisCache")
    public UserAuthCacheView getUserByUsernameFromCache(UUID companyId, String username) {
        try {
            var key = scopedKey("username", companyId, username);
            var value = redisTemplate.opsForValue().get(key);

            if (value == null || value.isBlank()) {
                return null;
            }

            return objectMapper.readValue(value, UserAuthCacheView.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeUserFromCacheByUsername(UUID companyId, String username) {
        try {
            redisTemplate.delete(scopedKey("username", companyId, username));
        } catch (Exception e) {
            log.warn("Falha ao remover usuario do cache | username={} | erro={}", username, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheUserByEmail(UUID companyId, String email, UserGetByEmailView user) {
        try {
            String key = scopedKey("email", companyId, email);
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, userTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear usuario | email={} | erro={}", email, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "scopedCacheFallback")
    @Retry(name = "redisCache")
    public UserAuthCacheView getUserByEmailFromCache(UUID companyId, String email) {
        try {
            var key = scopedKey("email", companyId, email);
            var value = redisTemplate.opsForValue().get(key);

            if (value == null || value.isBlank()) {
                return null;
            }

            return objectMapper.readValue(value, UserAuthCacheView.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeUserFromCacheByEmail(UUID companyId, String email) {
        try {
            redisTemplate.delete(scopedKey("email", companyId, email));
        } catch (Exception e) {
            log.warn("Falha ao remover usuario do cache | email={} | erro={}", email, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheUserByCodeUser(String codeUser, UserGetByCodeView user) {
        try {
            String key = authKey("codeuser", codeUser);
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, userTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear usuario | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "cacheFallback")
    @Retry(name = "redisCache")
    public UserAuthCacheView getUserByCodeUserFromCache(String codeUser) {
        try {
            var key = authKey("codeuser", codeUser);
            var value = redisTemplate.opsForValue().get(key);

            if (value == null || value.isBlank()) {
                return null;
            }

            return objectMapper.readValue(value, UserAuthCacheView.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeUserFromCacheByCodeUser(String codeUser) {
        try {
            redisTemplate.delete(authKey("codeuser", codeUser));
        } catch (Exception e) {
            log.warn("Falha ao remover usuario do cache | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheUserByIdExternal(String idUserExternal, UserAuthCacheView user) {
        try {
            String key = authKey("external", idUserExternal);
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, userTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear usuario | idExternal={} | erro={}", idUserExternal, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "cacheFallback")
    @Retry(name = "redisCache")
    public UserAuthCacheView getUserByIdExternalFromCache(String idUserExternal) {
        try {
            var key = authKey("external", idUserExternal);
            var value = redisTemplate.opsForValue().get(key);

            if (value == null || value.isBlank()) {
                return null;
            }

            return objectMapper.readValue(value, UserAuthCacheView.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeUserFromCacheByIdExternal(String idUserExternal) {
        try {
            redisTemplate.delete(authKey("external", idUserExternal));
        } catch (Exception e) {
            log.warn("Falha ao remover usuario do cache | idExternal={} | erro={}", idUserExternal, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheUserRoles(String codeUser, UserRolesCacheView userRoles) {
        try {
            String key = rolesKey(codeUser);
            String value = objectMapper.writeValueAsString(userRoles);
            redisTemplate.opsForValue().set(key, value, userRolesTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear roles do usuario | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "cacheRolesFallback")
    @Retry(name = "redisCache")
    public UserRolesCacheView getUserRolesFromCache(String codeUser) {
        try {
            var key = rolesKey(codeUser);
            var value = redisTemplate.opsForValue().get(key);

            if (value == null || value.isBlank()) {
                return null;
            }

            return objectMapper.readValue(value, UserRolesCacheView.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeUserRolesFromCache(String codeUser) {
        try {
            redisTemplate.delete(rolesKey(codeUser));
        } catch (Exception e) {
            log.warn("Falha ao remover roles do cache | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeUserFromCache(User user) {
        removeUserFromCacheByUsername(user.getCompanyId(), user.getUsername());
        removeUserFromCacheByEmail(user.getCompanyId(), user.getEmail());
        removeUserFromCacheByCodeUser(user.getCodeUser().toString());
    }

    @CircuitBreaker(name = "redisCache")
    public void clearAllUserCache() {
        try {
            var userPattern = basePrefix() + ":" + CONTEXT + ":*";
            var userKeys = redisTemplate.keys(userPattern);

            long deletedCount = 0;

            if (userKeys != null && !userKeys.isEmpty()) {
                deletedCount += redisTemplate.delete(userKeys);
            }

            if (userRolesCachePrefix != null && !userRolesCachePrefix.isBlank()) {
                var rolesPattern = userRolesCachePrefix.replaceAll(":+$", "") + ":*";
                var rolesKeys = redisTemplate.keys(rolesPattern);
                if (rolesKeys != null && !rolesKeys.isEmpty()) {
                    deletedCount += redisTemplate.delete(rolesKeys);
                }
            }

            if (deletedCount > 0) {
                log.info("Cache de usuarios limpo | chaves removidas={}", deletedCount);
            }
        } catch (Exception e) {
            log.warn("Falha ao limpar cache de usuarios | erro={}", e.getMessage());
        }
    }

    private String basePrefix() {
        if (userCachePrefix == null || userCachePrefix.isBlank()) {
            return "user_cache";
        }
        return userCachePrefix.replaceAll(":+$", "");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String authKey(String lookup, String id) {
        return basePrefix() + ":" + CONTEXT + ":" + lookup + ":" + normalize(id);
    }

    private String scopedKey(String kind, UUID companyId, String value) {
        return basePrefix() + ":" + CONTEXT + ":" + kind + ":" + companyId + ":" + normalize(value);
    }

    private String rolesKey(String codeUser) {
        return authKey("roles", codeUser);
    }

    private UserAuthCacheView cacheFallback(String param, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel, buscando do banco | param={} | erro={}",
            param, ex.getClass().getSimpleName());
        return null;
    }

    private UserAuthCacheView scopedCacheFallback(UUID companyId, String param, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel, buscando do banco | companyId={} | param={} | erro={}",
            companyId, param, ex.getClass().getSimpleName());
        return null;
    }

    private UserRolesCacheView cacheRolesFallback(String codeUser, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel, buscando roles do banco | codeUser={} | erro={}",
            codeUser, ex.getClass().getSimpleName());
        return null;
    }

}
