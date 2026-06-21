package com.keepguard.ms_auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.dto.role.RoleCacheView;
import com.keepguard.ms_auth.application.port.out.cache.RoleCachePort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Serviço de cache de roles com resiliência usando Redis.
 * 
 * <p>Implementa cache Redis com Circuit Breaker para garantir que
 * a aplicação continue funcionando mesmo quando o Redis está indisponível.
 * 
 * <p><b>Estratégia de Resiliência:</b>
 * <ul>
 *   <li><b>Leitura:</b> Circuit Breaker + Retry com fallback (retorna null)</li>
 *   <li><b>Escrita:</b> Circuit Breaker (fail silently, loga e continua)</li>
 * </ul>
 * 
 * @author KeepGuard Team
 * @version 1.0.78
 * @since 1.0.77
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleCacheService implements RoleCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.redis.ttl.role:604800}")
    private long roleTtlSeconds;

    @Value("${cache.redis.prefix.role:role_cache}")
    private String roleCachePrefix;

    @CircuitBreaker(name = "redisCache")
    public void cacheRoleById(String roleId, RoleCacheView role) {
        try {
            String key = roleCachePrefix + ":" + roleId;
            String value = objectMapper.writeValueAsString(role);
            redisTemplate.opsForValue().set(key, value, roleTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear role | roleId={} | erro={}", roleId, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "cacheFallback")
    @Retry(name = "redisCache")
    public RoleCacheView getRoleByIdFromCache(String roleId) {
        try {
            var key = "%s:%s".formatted(roleCachePrefix, roleId);
            var value = redisTemplate.opsForValue().get(key);
            
            if (value == null || value.isBlank()) {
                return null;
            }
            
            return objectMapper.readValue(value, RoleCacheView.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private RoleCacheView cacheFallback(String roleId, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel, buscando do banco | roleId={} | erro={}", 
            roleId, ex.getClass().getSimpleName());
        return null;
    }

    @CircuitBreaker(name = "redisCache")
    public void removeRoleFromCacheById(String roleId) {
        try {
            String key = roleCachePrefix + ":" + roleId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover role do cache | roleId={} | erro={}", roleId, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void clearAllRoleCache() {
        try {
            var pattern = roleCachePrefix + ":*";
            var keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                var deletedCount = redisTemplate.delete(keys);
                log.info("Cache de roles limpo | chaves removidas={}", deletedCount);
            }
        } catch (Exception e) {
            log.warn("Falha ao limpar cache de roles | erro={}", e.getMessage());
        }
    }

}