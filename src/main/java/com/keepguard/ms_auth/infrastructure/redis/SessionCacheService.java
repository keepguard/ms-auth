package com.keepguard.ms_auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.port.out.cache.SessionCachePort;
import com.keepguard.ms_auth.domain.entity.session.DeviceChallengeSession;
import com.keepguard.ms_auth.domain.entity.session.UserSession;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCacheService implements SessionCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_SESSIONS_PREFIX = "user:sessions:";
    private static final String DEVICE_CHALLENGE_PREFIX = "mfa:device_challenge:";
    private static final String DEVICE_BLACKLIST_PREFIX = "device:blacklist:";
    private static final String QUICK_REVOKE_PREFIX = "auth:quick_revoke:";

    @Override
    @CircuitBreaker(name = "redisCache")
    public void saveUserSession(UserSession session, long ttlSeconds) {
        try {
            String key = USER_SESSIONS_PREFIX + session.getCodeUser() + ":" + session.getDeviceId();
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            log.info("Sessão de dispositivo salva | codeUser={} | deviceId={}", session.getCodeUser(), session.getDeviceId());
        } catch (Exception e) {
            log.warn("Falha ao salvar sessão de dispositivo | codeUser={} | erro={}", session.getCodeUser(), e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public Optional<UserSession> getUserSession(String codeUser, String deviceId) {
        try {
            String key = USER_SESSIONS_PREFIX + codeUser + ":" + deviceId;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null && !json.isBlank()) {
                return Optional.of(objectMapper.readValue(json, UserSession.class));
            }
        } catch (Exception e) {
            log.warn("Falha ao buscar sessão de dispositivo | codeUser={} | deviceId={} | erro={}", codeUser, deviceId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public List<UserSession> listUserSessions(String codeUser) {
        List<UserSession> sessions = new ArrayList<>();
        try {
            String pattern = USER_SESSIONS_PREFIX + codeUser + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json != null && !json.isBlank()) {
                        sessions.add(objectMapper.readValue(json, UserSession.class));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao listar sessões de dispositivos | codeUser={} | erro={}", codeUser, e.getMessage());
        }
        return sessions;
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeUserSession(String codeUser, String deviceId) {
        try {
            String key = USER_SESSIONS_PREFIX + codeUser + ":" + deviceId;
            redisTemplate.delete(key);
            log.info("Sessão de dispositivo removida | codeUser={} | deviceId={}", codeUser, deviceId);
        } catch (Exception e) {
            log.warn("Falha ao remover sessão de dispositivo | codeUser={} | deviceId={} | erro={}", codeUser, deviceId, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeAllUserSessionsExceptCurrent(String codeUser, String currentDeviceId) {
        try {
            String pattern = USER_SESSIONS_PREFIX + codeUser + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                String currentKey = USER_SESSIONS_PREFIX + codeUser + ":" + currentDeviceId;
                for (String key : keys) {
                    if (!key.equals(currentKey)) {
                        redisTemplate.delete(key);
                    }
                }
                log.info("Todas as outras sessões de dispositivos removidas | codeUser={} | currentDeviceId={}", codeUser, currentDeviceId);
            }
        } catch (Exception e) {
            log.warn("Falha ao remover outras sessões de dispositivos | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeAllUserSessions(String codeUser) {
        try {
            String pattern = USER_SESSIONS_PREFIX + codeUser + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Todas as sessões de dispositivos removidas | codeUser={}", codeUser);
            }
        } catch (Exception e) {
            log.warn("Falha ao remover todas as sessões de dispositivos | codeUser={} | erro={}", codeUser, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void saveDeviceChallenge(DeviceChallengeSession challenge, long ttlSeconds) {
        try {
            String key = DEVICE_CHALLENGE_PREFIX + challenge.getChallengeSessionId();
            String json = objectMapper.writeValueAsString(challenge);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            log.info("Desafio de dispositivo salvo no Redis | challengeSessionId={}", challenge.getChallengeSessionId());
        } catch (Exception e) {
            log.warn("Falha ao salvar desafio de dispositivo no Redis | challengeSessionId={} | erro={}", challenge.getChallengeSessionId(), e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public Optional<DeviceChallengeSession> getDeviceChallenge(String challengeSessionId) {
        try {
            String key = DEVICE_CHALLENGE_PREFIX + challengeSessionId;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null && !json.isBlank()) {
                return Optional.of(objectMapper.readValue(json, DeviceChallengeSession.class));
            }
        } catch (Exception e) {
            log.warn("Falha ao buscar desafio de dispositivo no Redis | challengeSessionId={} | erro={}", challengeSessionId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeDeviceChallenge(String challengeSessionId) {
        try {
            String key = DEVICE_CHALLENGE_PREFIX + challengeSessionId;
            redisTemplate.delete(key);
            log.info("Desafio de dispositivo removido do Redis | challengeSessionId={}", challengeSessionId);
        } catch (Exception e) {
            log.warn("Falha ao remover desafio de dispositivo | challengeSessionId={} | erro={}", challengeSessionId, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void addToBlacklist(com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry entry, long ttlSeconds) {
        try {
            String key = DEVICE_BLACKLIST_PREFIX + entry.getCodeUser() + ":" + entry.getDeviceId();
            String json = objectMapper.writeValueAsString(entry);
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, json);
            }
            log.info("Dispositivo adicionado à blacklist | codeUser={} | deviceId={}", entry.getCodeUser(), entry.getDeviceId());
        } catch (Exception e) {
            log.warn("Falha ao adicionar dispositivo à blacklist | codeUser={} | deviceId={} | erro={}", entry.getCodeUser(), entry.getDeviceId(), e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public boolean isDeviceBlacklisted(String codeUser, String deviceId) {
        if (codeUser == null || deviceId == null) {
            return false;
        }
        try {
            String key = DEVICE_BLACKLIST_PREFIX + codeUser + ":" + deviceId;
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.warn("Falha ao verificar blacklist de dispositivo | codeUser={} | deviceId={} | erro={}", codeUser, deviceId, e.getMessage());
            return false;
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public List<com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry> listBlacklistedDevices(String codeUser) {
        List<com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry> list = new ArrayList<>();
        try {
            String pattern = DEVICE_BLACKLIST_PREFIX + codeUser + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json != null && !json.isBlank()) {
                        list.add(objectMapper.readValue(json, com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry.class));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao listar dispositivos da blacklist | codeUser={} | erro={}", codeUser, e.getMessage());
        }
        return list;
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeFromBlacklist(String codeUser, String deviceId) {
        try {
            String key = DEVICE_BLACKLIST_PREFIX + codeUser + ":" + deviceId;
            redisTemplate.delete(key);
            log.info("Dispositivo removido da blacklist | codeUser={} | deviceId={}", codeUser, deviceId);
        } catch (Exception e) {
            log.warn("Falha ao remover dispositivo da blacklist | codeUser={} | deviceId={} | erro={}", codeUser, deviceId, e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void saveQuickRevokeToken(com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken quickRevokeToken, long ttlSeconds) {
        try {
            String key = QUICK_REVOKE_PREFIX + quickRevokeToken.getToken();
            String json = objectMapper.writeValueAsString(quickRevokeToken);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            log.info("Token de revogação rápida salvo | token={} | codeUser={} | deviceId={}", quickRevokeToken.getToken(), quickRevokeToken.getCodeUser(), quickRevokeToken.getDeviceId());
        } catch (Exception e) {
            log.warn("Falha ao salvar token de revogação rápida | erro={}", e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public Optional<com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken> getQuickRevokeToken(String token) {
        try {
            String key = QUICK_REVOKE_PREFIX + token;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null && !json.isBlank()) {
                return Optional.of(objectMapper.readValue(json, com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken.class));
            }
        } catch (Exception e) {
            log.warn("Falha ao buscar token de revogação rápida | token={} | erro={}", token, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    @CircuitBreaker(name = "redisCache")
    public void removeQuickRevokeToken(String token) {
        try {
            String key = QUICK_REVOKE_PREFIX + token;
            redisTemplate.delete(key);
            log.info("Token de revogação rápida removido | token={}", token);
        } catch (Exception e) {
            log.warn("Falha ao remover token de revogação rápida | token={} | erro={}", token, e.getMessage());
        }
    }
}
