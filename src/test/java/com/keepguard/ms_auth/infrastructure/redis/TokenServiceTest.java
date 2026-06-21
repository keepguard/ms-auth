package com.keepguard.ms_auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisOperations;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private TokenCacheService tokenCacheService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisOperations<String, String> redisOperations;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        tokenCacheService = new TokenCacheService(redisTemplate, objectMapper);
        
        // Use reflection to set the @Value fields
        try {
            var field = TokenCacheService.class.getDeclaredField("tokenPrefix");
            field.setAccessible(true);
            field.set(tokenCacheService, "tokenlogin:");
            
            field = TokenCacheService.class.getDeclaredField("resetTokenPrefix");
            field.setAccessible(true);
            field.set(tokenCacheService, "resetpassword:");
            
            field = TokenCacheService.class.getDeclaredField("tokenTtlSeconds");
            field.setAccessible(true);
            field.set(tokenCacheService, 3600L);
            
            field = TokenCacheService.class.getDeclaredField("resetTokenTtlSeconds");
            field.setAccessible(true);
            field.set(tokenCacheService, 900L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set @Value fields", e);
        }
        
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.hasKey(anyString())).thenReturn(false);
        lenient().when(redisTemplate.keys(anyString())).thenReturn(Set.of());
    }

    @Test
    @DisplayName("Deve salvar token com sucesso")
    void shouldSaveTokenSuccessfully() {
        // Given
        String codeUser = "user123";
        String token = "jwt-token-123";
        long ttlMillis = 3600000L;

        // When
        tokenCacheService.saveToken(codeUser, token, ttlMillis);

        // Then
        verify(valueOperations).set(eq("tokenlogin:user123:jwt-token-123"), anyString(), eq(ttlMillis), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Deve verificar se token é válido")
    void shouldCheckIfTokenIsValid() {
        // Given
        String codeUser = "user123";
        String token = "jwt-token-123";
        when(redisTemplate.hasKey("tokenlogin:user123:jwt-token-123")).thenReturn(true);

        // When
        boolean isValid = tokenCacheService.isTokenValid(codeUser, token);

        // Then
        assertTrue(isValid);
        verify(redisTemplate).hasKey("tokenlogin:user123:jwt-token-123");
    }

    @Test
    @DisplayName("Deve retornar false quando token não existe")
    void shouldReturnFalseWhenTokenDoesNotExist() {
        // Given
        String codeUser = "user123";
        String token = "invalid-token";
        when(redisTemplate.hasKey("tokenlogin:user123:invalid-token")).thenReturn(false);

        // When
        boolean isValid = tokenCacheService.isTokenValid(codeUser, token);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve remover todos os tokens de um usuário")
    void shouldRemoveAllTokensForUser() {
        // Given
        String codeUser = "user123";
        Set<String> keys = Set.of("tokenlogin:user123:token1", "tokenlogin:user123:token2");
        when(redisTemplate.keys("tokenlogin:user123:*")).thenReturn(keys);

        // When
        tokenCacheService.removeAllTokens(codeUser);

        // Then
        verify(redisTemplate).keys("tokenlogin:user123:*");
        verify(redisTemplate).delete(keys);
    }

    @Test
    @DisplayName("Deve remover token específico")
    void shouldRemoveSpecificToken() {
        // Given
        String codeUser = "user123";
        String token = "jwt-token-123";

        // When
        tokenCacheService.removeToken(codeUser, token);

        // Then
        verify(redisTemplate).delete("tokenlogin:user123:jwt-token-123");
    }

    @Test
    @DisplayName("Deve salvar reset token com sucesso")
    void shouldSaveResetTokenSuccessfully() {
        // Given
        String codeUser = "user123";
        String messageType = "EMAIL";
        String templateType = "RECUPERACAO_SENHA";
        String token = "reset-token-123";
        long ttlMillis = 900000L;

        // When
        tokenCacheService.saveToken(codeUser, messageType, templateType, token, ttlMillis);

        // Then
        verify(valueOperations).set(eq("resetpassword:user123:EMAIL:RECUPERACAO_SENHA"), anyString(), eq(ttlMillis), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Deve verificar se reset token é válido")
    void shouldCheckIfResetTokenIsValid() {
        // Given
        String codeUser = "user123";
        String messageType = "EMAIL";
        String templateType = "RECUPERACAO_SENHA";
        String token = "reset-token-123";
        String tokenJson = "{\"token\":\"reset-token-123\",\"createdAt\":\"2023-01-01T00:00:00Z\",\"expiresAt\":\"2023-01-01T01:00:00Z\"}";
        when(valueOperations.get("resetpassword:user123:EMAIL:RECUPERACAO_SENHA")).thenReturn(tokenJson);

        // When
        boolean isValid = tokenCacheService.isResetTokenValid(codeUser, messageType, templateType, token);

        // Then
        assertTrue(isValid);
        verify(valueOperations).get("resetpassword:user123:EMAIL:RECUPERACAO_SENHA");
    }

    @Test
    @DisplayName("Deve retornar false quando reset token não existe")
    void shouldReturnFalseWhenResetTokenDoesNotExist() {
        // Given
        String codeUser = "user123";
        String messageType = "EMAIL";
        String templateType = "RECUPERACAO_SENHA";
        String token = "invalid-reset-token";
        when(valueOperations.get("resetpassword:user123:EMAIL:RECUPERACAO_SENHA")).thenReturn(null);

        // When
        boolean isValid = tokenCacheService.isResetTokenValid(codeUser, messageType, templateType, token);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve retornar false quando reset token é inválido")
    void shouldReturnFalseWhenResetTokenIsInvalid() {
        // Given
        String codeUser = "user123";
        String messageType = "EMAIL";
        String templateType = "RECUPERACAO_SENHA";
        String token = "wrong-token";
        String tokenJson = "{\"token\":\"different-token\",\"createdAt\":\"2023-01-01T00:00:00Z\",\"expiresAt\":\"2023-01-01T01:00:00Z\"}";
        when(valueOperations.get("resetpassword:user123:EMAIL:RECUPERACAO_SENHA")).thenReturn(tokenJson);

        // When
        boolean isValid = tokenCacheService.isResetTokenValid(codeUser, messageType, templateType, token);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve remover reset token")
    void shouldRemoveResetToken() {
        // Given
        String codeUser = "user123";
        String messageType = "EMAIL";
        String templateType = "RECUPERACAO_SENHA";

        // When
        tokenCacheService.removeResetToken(codeUser, messageType, templateType);

        // Then
        verify(redisTemplate).delete("resetpassword:user123:EMAIL:RECUPERACAO_SENHA");
    }

    @Test
    @DisplayName("Deve falhar silenciosamente quando falha ao serializar token (resiliencia)")
    void shouldFailSilentlyWhenTokenSerializationFails() {
        // Given
        String codeUser = "user123";
        String token = "jwt-token-123";
        long ttlMillis = 3600000L;
        
        // Mock ObjectMapper to throw exception
        ObjectMapper faultyMapper = mock(ObjectMapper.class);
        try {
            when(faultyMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Serialization error") {});
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // This won't happen in the test
        }
        
        TokenCacheService faultyTokenCacheService = new TokenCacheService(redisTemplate, faultyMapper);
        try {
            var field = TokenCacheService.class.getDeclaredField("tokenPrefix");
            field.setAccessible(true);
            field.set(faultyTokenCacheService, "tokenlogin:");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set @Value fields", e);
        }

        // When & Then - Agora com Circuit Breaker, falha silenciosamente (nao lanca excecao)
        assertDoesNotThrow(() -> 
            faultyTokenCacheService.saveToken(codeUser, token, ttlMillis));
    }

    @Test
    @DisplayName("Deve falhar silenciosamente quando falha ao serializar reset token (resiliencia)")
    void shouldFailSilentlyWhenResetTokenSerializationFails() {
        // Given
        String codeUser = "user123";
        String token = "reset-token-123";
        long ttlMillis = 900000L;
        
        // Mock ObjectMapper to throw exception
        ObjectMapper faultyMapper = mock(ObjectMapper.class);
        try {
            when(faultyMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Serialization error") {});
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // This won't happen in the test
        }
        
        TokenCacheService faultyTokenCacheService = new TokenCacheService(redisTemplate, faultyMapper);
        try {
            var field = TokenCacheService.class.getDeclaredField("resetTokenPrefix");
            field.setAccessible(true);
            field.set(faultyTokenCacheService, "resetpassword:");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set @Value fields", e);
        }

        // When & Then - Agora com Circuit Breaker, falha silenciosamente (nao lanca excecao)
        assertDoesNotThrow(() -> 
            faultyTokenCacheService.saveToken(codeUser, "EMAIL", "RECUPERACAO_SENHA", token, ttlMillis));
    }
}
