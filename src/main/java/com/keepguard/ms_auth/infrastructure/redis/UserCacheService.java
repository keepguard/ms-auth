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

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService implements UserCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.redis.ttl.user:604800}")
    private long userTtlSeconds;

    @Value("${cache.redis.ttl.user-roles:604800}")
    private long userRolesTtlSeconds;

    @Value("${cache.redis.prefix.user:user_cache}")
    private String userCachePrefix;

    @Value("${cache.redis.prefix.user-roles:user_roles_cache}")
    private String userRolesCachePrefix;

    @CircuitBreaker(name = "redisCache")
    public void cacheUserByUsername(String username, UserGetByUsernameView user) {
        try {
            String key = userCachePrefix + ":username:" + username;
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, userTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear usuario | username={} | erro={}", username, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "cacheFallback")
    @Retry(name = "redisCache")
    public UserAuthCacheView getUserByUsernameFromCache(String username) {
        try {
            var key = "%s:username:%s".formatted(userCachePrefix, username);
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
    public void removeUserFromCacheByUsername(String username) {
        try {
            String key = userCachePrefix + ":username:" + username;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover usuario do cache | username={} | erro={}", username, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheUserByEmail(String email, UserGetByEmailView user) {
        try {
            String key = userCachePrefix + ":email:" + email.toLowerCase().trim();
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, userTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear usuario | email={} | erro={}", email, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "cacheFallback")
    @Retry(name = "redisCache")
    public UserAuthCacheView getUserByEmailFromCache(String email) {
        try {
            var key = "%s:email:%s".formatted(userCachePrefix, email.toLowerCase().trim());
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
    public void removeUserFromCacheByEmail(String email) {
        try {
            String key = userCachePrefix + ":email:" + email.toLowerCase().trim();
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover usuario do cache | email={} | erro={}", email, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheUserByCodeUser(String codeUser, UserGetByCodeView user) {
        try {
            String key = userCachePrefix + ":codeuser:" + codeUser;
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
            var key = "%s:codeuser:%s".formatted(userCachePrefix, codeUser);
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
            String key = userCachePrefix + ":codeuser:" + codeUser;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover usuario do cache | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheUserByIdExternal(String idUserExternal, UserAuthCacheView user) {
        try {
            String key = userCachePrefix + ":external:" + idUserExternal;
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
            var key = "%s:external:%s".formatted(userCachePrefix, idUserExternal);
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
            String key = userCachePrefix + ":external:" + idUserExternal;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover usuario do cache | idExternal={} | erro={}", idUserExternal, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheUserRoles(String codeUser, UserRolesCacheView userRoles) {
        try {
            String key = userRolesCachePrefix + ":" + codeUser;
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
            var key = "%s:%s".formatted(userRolesCachePrefix, codeUser);
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
            String key = userRolesCachePrefix + ":" + codeUser;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover roles do cache | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeUserFromCache(User user) {
        removeUserFromCacheByUsername(user.getUsername());
        removeUserFromCacheByEmail(user.getEmail());
        removeUserFromCacheByCodeUser(user.getCodeUser().toString());
    }

    @CircuitBreaker(name = "redisCache")
    public void clearAllUserCache() {
        try {
            var userPattern = userCachePrefix + ":*";
            var rolesPattern = userRolesCachePrefix + ":*";
            
            var userKeys = redisTemplate.keys(userPattern);
            var rolesKeys = redisTemplate.keys(rolesPattern);
            
            long deletedCount = 0;
            
            if (userKeys != null && !userKeys.isEmpty()) {
                deletedCount += redisTemplate.delete(userKeys);
            }
            
            if (rolesKeys != null && !rolesKeys.isEmpty()) {
                deletedCount += redisTemplate.delete(rolesKeys);
            }
            
            if (deletedCount > 0) {
                log.info("Cache de usuarios limpo | chaves removidas={}", deletedCount);
            }
        } catch (Exception e) {
            log.warn("Falha ao limpar cache de usuarios | erro={}", e.getMessage());
        }
    }

    private UserAuthCacheView cacheFallback(String param, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel, buscando do banco | param={} | erro={}", 
            param, ex.getClass().getSimpleName());
        return null;
    }

    private UserRolesCacheView cacheRolesFallback(String codeUser, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel, buscando roles do banco | codeUser={} | erro={}", 
            codeUser, ex.getClass().getSimpleName());
        return null;
    }

}