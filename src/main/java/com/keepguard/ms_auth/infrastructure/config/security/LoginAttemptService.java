package com.keepguard.ms_auth.infrastructure.config.security;

import com.keepguard.ms_auth.application.service.exception.AccountLockedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;

    // Configurações de Rate Limiting via @Value
    @Value("${security.rate-limiting.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.rate-limiting.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Value("${security.rate-limiting.attempts-ttl-hours:1}")
    private int attemptsTtlHours;

    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts:";
    private static final String ACCOUNT_LOCKED_PREFIX = "account_locked:";

    public void recordFailedAttempt(String username) {
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;

        // Verificar se a conta está bloqueada
        if (isAccountLocked(username)) {
            log.warn("Tentativa de login para usuário bloqueado: {}", username);
            throw new AccountLockedException("Conta temporariamente bloqueada. Tente novamente em " +
                    getRemainingLockoutTime(username) + " minutos.");
        }

        // Incrementar tentativas
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts == 1) {
            // Primeira tentativa - definir TTL
            redisTemplate.expire(attemptsKey, attemptsTtlHours, TimeUnit.HOURS);
        }

        log.info("Tentativa de login falhou para usuário: {} (tentativa {}/{})",
                username, attempts, maxAttempts);

        // Verificar se atingiu o limite
        if (attempts >= maxAttempts) {
            lockAccount(username);
            log.warn("Conta bloqueada para usuário: {} após {} tentativas", username, maxAttempts);
            throw new AccountLockedException("Muitas tentativas de login. Conta bloqueada por " +
                    lockoutDurationMinutes + " minutos.");
        }
    }

    public void recordSuccessfulAttempt(String username) {
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;
        String lockedKey = ACCOUNT_LOCKED_PREFIX + username;

        // Limpar tentativas e bloqueio
        redisTemplate.delete(attemptsKey);
        redisTemplate.delete(lockedKey);

        log.info("Login bem-sucedido para usuário: {}. Tentativas e bloqueios removidos.", username);
    }

    public boolean isAccountLocked(String username) {
        String lockedKey = ACCOUNT_LOCKED_PREFIX + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockedKey));
    }

    private void lockAccount(String username) {
        String lockedKey = ACCOUNT_LOCKED_PREFIX + username;
        redisTemplate.opsForValue().set(lockedKey, LocalDateTime.now().toString(),
                lockoutDurationMinutes, TimeUnit.MINUTES);
    }

    public long getRemainingLockoutTime(String username) {
        String lockedKey = ACCOUNT_LOCKED_PREFIX + username;
        Long ttl = redisTemplate.getExpire(lockedKey, TimeUnit.MINUTES);
        return ttl != null ? ttl : 0;
    }

    public int getRemainingAttempts(String username) {
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;
        String attempts = redisTemplate.opsForValue().get(attemptsKey);
        if (attempts == null) {
            return maxAttempts;
        }
        int currentAttempts = Integer.parseInt(attempts);
        return Math.max(0, maxAttempts - currentAttempts);
    }

    public void forceUnlockAccount(String username) {
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;
        String lockedKey = ACCOUNT_LOCKED_PREFIX + username;

        redisTemplate.delete(attemptsKey);
        redisTemplate.delete(lockedKey);

        log.info("Conta desbloqueada forçadamente para usuário: {}", username);
    }
}