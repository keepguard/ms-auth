package com.keepguard.ms_auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.dto.role.RoleCacheView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Role Cache Service Adapter Tests")
class RoleCacheServiceTest {

    private RoleCacheService roleCacheServiceAdapter;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private RoleCacheView roleCacheViewDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        roleCacheServiceAdapter = new RoleCacheService(redisTemplate, objectMapper);
        
        // Use reflection to set the @Value fields
        try {
            setFieldValue("roleTtlSeconds", 604800L);
            setFieldValue("roleCachePrefix", "role_cache");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set @Value fields", e);
        }
        
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.delete(anyString())).thenReturn(true);

        // Setup test data
        roleCacheViewDTO = new RoleCacheView(
            UUID.randomUUID(),
            "ADMIN",
            "Administrator role",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    private void setFieldValue(String fieldName, Object value) throws Exception {
        var field = RoleCacheService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(roleCacheServiceAdapter, value);
    }

    @Test
    @DisplayName("Deve cachear role por ID com sucesso")
    void shouldCacheRoleByIdSuccessfully() {
        // When
        roleCacheServiceAdapter.cacheRoleById("role123", roleCacheViewDTO);

        // Then
        verify(valueOperations).set(eq("role_cache:role123"), anyString(), eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve recuperar role por ID do cache")
    void shouldGetRoleByIdFromCache() throws Exception {
        // Given
        String roleJson = objectMapper.writeValueAsString(roleCacheViewDTO);
        when(valueOperations.get("role_cache:role123")).thenReturn(roleJson);

        // When
        RoleCacheView result = roleCacheServiceAdapter.getRoleByIdFromCache("role123");

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.name());
        assertEquals("Administrator role", result.description());
    }

    @Test
    @DisplayName("Deve remover role do cache por ID")
    void shouldRemoveRoleFromCacheById() {
        // When
        roleCacheServiceAdapter.removeRoleFromCacheById("role123");

        // Then
        verify(redisTemplate).delete("role_cache:role123");
    }

    @Test
    @DisplayName("Deve retornar null quando role não existe no cache")
    void shouldReturnNullWhenRoleDoesNotExistInCache() {
        // Given
        when(valueOperations.get("role_cache:nonexistent")).thenReturn(null);

        // When
        RoleCacheView result = roleCacheServiceAdapter.getRoleByIdFromCache("nonexistent");

        // Then
        assertNull(result);
    }

}
