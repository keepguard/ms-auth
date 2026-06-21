package com.keepguard.ms_auth.infrastructure.config.security;

import com.keepguard.ms_auth.application.service.exception.AccountLockedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Login Attempt Service Tests")
class LoginAttemptServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private LoginAttemptService loginAttemptService;

    private final String username = "testuser";
    private final int maxAttempts = 5;
    private final int lockoutDurationMinutes = 15;
    private final int attemptsTtlHours = 1;

    @BeforeEach
    void setUp() {
        // Set private fields using reflection
        ReflectionTestUtils.setField(loginAttemptService, "maxAttempts", maxAttempts);
        ReflectionTestUtils.setField(loginAttemptService, "lockoutDurationMinutes", lockoutDurationMinutes);
        ReflectionTestUtils.setField(loginAttemptService, "attemptsTtlHours", attemptsTtlHours);
        
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Deve registrar tentativa falhada com sucesso")
    void shouldRecordFailedAttemptSuccessfully() {
        // Given
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // When
        assertDoesNotThrow(() -> loginAttemptService.recordFailedAttempt(username));

        // Then
        verify(valueOperations).increment("login_attempts:" + username);
        verify(redisTemplate).expire("login_attempts:" + username, attemptsTtlHours, TimeUnit.HOURS);
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta está bloqueada")
    void shouldThrowExceptionWhenAccountIsLocked() {
        // Given
        when(redisTemplate.hasKey("account_locked:" + username)).thenReturn(true);
        when(redisTemplate.getExpire("account_locked:" + username, TimeUnit.MINUTES)).thenReturn(10L);

        // When & Then
        AccountLockedException exception = assertThrows(AccountLockedException.class, 
            () -> loginAttemptService.recordFailedAttempt(username));
        
        assertTrue(exception.getMessage().contains("Conta temporariamente bloqueada"));
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    @DisplayName("Deve bloquear conta após máximo de tentativas")
    void shouldLockAccountAfterMaxAttempts() {
        // Given
        when(redisTemplate.hasKey("account_locked:" + username)).thenReturn(false);
        when(valueOperations.increment("login_attempts:" + username)).thenReturn((long) maxAttempts);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // When & Then
        AccountLockedException exception = assertThrows(AccountLockedException.class, 
            () -> loginAttemptService.recordFailedAttempt(username));
        
        assertTrue(exception.getMessage().contains("Muitas tentativas de login"));
        verify(valueOperations).set(eq("account_locked:" + username), anyString(), 
            eq((long) lockoutDurationMinutes), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Deve registrar login bem-sucedido e limpar tentativas")
    void shouldRecordSuccessfulAttemptAndClearAttempts() {
        // When
        loginAttemptService.recordSuccessfulAttempt(username);

        // Then
        verify(redisTemplate).delete("login_attempts:" + username);
        verify(redisTemplate).delete("account_locked:" + username);
    }

    @Test
    @DisplayName("Deve verificar se conta está bloqueada")
    void shouldCheckIfAccountIsLocked() {
        // Given
        when(redisTemplate.hasKey("account_locked:" + username)).thenReturn(true);

        // When
        boolean isLocked = loginAttemptService.isAccountLocked(username);

        // Then
        assertTrue(isLocked);
        verify(redisTemplate).hasKey("account_locked:" + username);
    }

    @Test
    @DisplayName("Deve verificar se conta não está bloqueada")
    void shouldCheckIfAccountIsNotLocked() {
        // Given
        when(redisTemplate.hasKey("account_locked:" + username)).thenReturn(false);

        // When
        boolean isLocked = loginAttemptService.isAccountLocked(username);

        // Then
        assertFalse(isLocked);
        verify(redisTemplate).hasKey("account_locked:" + username);
    }

    @Test
    @DisplayName("Deve obter tempo restante de bloqueio")
    void shouldGetRemainingLockoutTime() {
        // Given
        long remainingMinutes = 10L;
        when(redisTemplate.getExpire("account_locked:" + username, TimeUnit.MINUTES)).thenReturn(remainingMinutes);

        // When
        long result = loginAttemptService.getRemainingLockoutTime(username);

        // Then
        assertEquals(remainingMinutes, result);
        verify(redisTemplate).getExpire("account_locked:" + username, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Deve retornar zero quando TTL é nulo")
    void shouldReturnZeroWhenTtlIsNull() {
        // Given
        when(redisTemplate.getExpire("account_locked:" + username, TimeUnit.MINUTES)).thenReturn(null);

        // When
        long result = loginAttemptService.getRemainingLockoutTime(username);

        // Then
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Deve obter tentativas restantes quando há tentativas")
    void shouldGetRemainingAttemptsWhenThereAreAttempts() {
        // Given
        int currentAttempts = 3;
        when(valueOperations.get("login_attempts:" + username)).thenReturn(String.valueOf(currentAttempts));

        // When
        int result = loginAttemptService.getRemainingAttempts(username);

        // Then
        assertEquals(maxAttempts - currentAttempts, result);
        verify(valueOperations).get("login_attempts:" + username);
    }

    @Test
    @DisplayName("Deve retornar máximo de tentativas quando não há tentativas")
    void shouldReturnMaxAttemptsWhenNoAttempts() {
        // Given
        when(valueOperations.get("login_attempts:" + username)).thenReturn(null);

        // When
        int result = loginAttemptService.getRemainingAttempts(username);

        // Then
        assertEquals(maxAttempts, result);
        verify(valueOperations).get("login_attempts:" + username);
    }

    @Test
    @DisplayName("Deve retornar zero quando tentativas excedem o máximo")
    void shouldReturnZeroWhenAttemptsExceedMax() {
        // Given
        int currentAttempts = 10; // More than maxAttempts
        when(valueOperations.get("login_attempts:" + username)).thenReturn(String.valueOf(currentAttempts));

        // When
        int result = loginAttemptService.getRemainingAttempts(username);

        // Then
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Deve forçar desbloqueio de conta")
    void shouldForceUnlockAccount() {
        // When
        loginAttemptService.forceUnlockAccount(username);

        // Then
        verify(redisTemplate).delete("login_attempts:" + username);
        verify(redisTemplate).delete("account_locked:" + username);
    }

    @Test
    @DisplayName("Deve registrar múltiplas tentativas falhadas")
    void shouldRecordMultipleFailedAttempts() {
        // Given
        when(redisTemplate.hasKey("account_locked:" + username)).thenReturn(false);
        when(valueOperations.increment("login_attempts:" + username))
            .thenReturn(1L)
            .thenReturn(2L)
            .thenReturn(3L);

        // When
        assertDoesNotThrow(() -> {
            loginAttemptService.recordFailedAttempt(username);
            loginAttemptService.recordFailedAttempt(username);
            loginAttemptService.recordFailedAttempt(username);
        });

        // Then
        verify(valueOperations, times(3)).increment("login_attempts:" + username);
        verify(redisTemplate, times(1)).expire("login_attempts:" + username, attemptsTtlHours, TimeUnit.HOURS);
    }

    @Test
    @DisplayName("Deve definir TTL apenas na primeira tentativa")
    void shouldSetTtlOnlyOnFirstAttempt() {
        // Given
        when(redisTemplate.hasKey("account_locked:" + username)).thenReturn(false);
        when(valueOperations.increment("login_attempts:" + username))
            .thenReturn(1L)
            .thenReturn(2L);

        // When
        loginAttemptService.recordFailedAttempt(username);
        loginAttemptService.recordFailedAttempt(username);

        // Then
        verify(redisTemplate, times(1)).expire("login_attempts:" + username, attemptsTtlHours, TimeUnit.HOURS);
    }

    @Test
    @DisplayName("Deve lidar com exceção durante incremento")
    void shouldHandleExceptionDuringIncrement() {
        // Given
        when(redisTemplate.hasKey("account_locked:" + username)).thenReturn(false);
        when(valueOperations.increment("login_attempts:" + username)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> loginAttemptService.recordFailedAttempt(username));
    }

    @Test
    @DisplayName("Deve lidar com exceção durante verificação de bloqueio")
    void shouldHandleExceptionDuringLockCheck() {
        // Given
        when(redisTemplate.hasKey("account_locked:" + username)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> loginAttemptService.recordFailedAttempt(username));
    }
}
