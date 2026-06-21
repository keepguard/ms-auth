package com.keepguard.ms_auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.dto.user.UserAuthCacheView;
import com.keepguard.ms_auth.application.dto.user.UserGetByUsernameView;
import com.keepguard.ms_auth.application.dto.user.UserGetByEmailView;
import com.keepguard.ms_auth.application.dto.user.UserGetByCodeView;
import com.keepguard.ms_auth.application.dto.user.UserRolesCacheView;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Cache Service Adapter Tests")
class UserCacheServiceTest {

    private UserCacheService userCacheServiceAdapter;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private UserAuthCacheView userAuthCacheViewDTO;
    private UserGetByUsernameView userGetByUsernameView;
    private UserGetByEmailView userGetByEmailView;
    private UserGetByCodeView userGetByCodeView;
    private UserRolesCacheView userRolesCacheViewDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        userCacheServiceAdapter = new UserCacheService(redisTemplate, objectMapper);
        
        // Use reflection to set the @Value fields
        try {
            setFieldValue("userTtlSeconds", 604800L);
            setFieldValue("userRolesTtlSeconds", 604800L);
            setFieldValue("userCachePrefix", "user_cache");
            setFieldValue("userRolesCachePrefix", "user_roles_cache");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set @Value fields", e);
        }
        
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.delete(anyString())).thenReturn(true);

        // Setup test data
        userAuthCacheViewDTO = new UserAuthCacheView(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            "hashedPassword",
            UserStatus.ACTIVE,
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        userRolesCacheViewDTO = new UserRolesCacheView(
            UUID.randomUUID(),
            List.of("ADMIN", "USER")
        );
        
        userGetByUsernameView = new UserGetByUsernameView(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            "Test User",
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ADMIN", "USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );
        
        userGetByEmailView = new UserGetByEmailView(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            "Test User",
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ADMIN", "USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );
        
        userGetByCodeView = new UserGetByCodeView(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            "Test User",
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ADMIN", "USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );
    }

    private void setFieldValue(String fieldName, Object value) throws Exception {
        var field = UserCacheService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(userCacheServiceAdapter, value);
    }

    // By Username Tests
    @Test
    @DisplayName("Deve cachear usuário por username com sucesso")
    void shouldCacheUserByUsernameSuccessfully() {
        // When
        userCacheServiceAdapter.cacheUserByUsername("testuser", userGetByUsernameView);

        // Then
        verify(valueOperations).set(eq("user_cache:username:testuser"), anyString(), eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve recuperar usuário por username do cache")
    void shouldGetUserByUsernameFromCache() throws Exception {
        // Given
        String userJson = objectMapper.writeValueAsString(userAuthCacheViewDTO);
        when(valueOperations.get("user_cache:username:testuser")).thenReturn(userJson);

        // When
        UserAuthCacheView result = userCacheServiceAdapter.getUserByUsernameFromCache("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
    }

    @Test
    @DisplayName("Deve remover usuário do cache por username")
    void shouldRemoveUserFromCacheByUsername() {
        // When
        userCacheServiceAdapter.removeUserFromCacheByUsername("testuser");

        // Then
        verify(redisTemplate).delete("user_cache:username:testuser");
    }

    // By Email Tests
    @Test
    @DisplayName("Deve cachear usuário por email com sucesso")
    void shouldCacheUserByEmailSuccessfully() {
        // When
        userCacheServiceAdapter.cacheUserByEmail("test@example.com", userGetByEmailView);

        // Then
        verify(valueOperations).set(eq("user_cache:email:test@example.com"), anyString(), eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve recuperar usuário por email do cache")
    void shouldGetUserByEmailFromCache() throws Exception {
        // Given
        String userJson = objectMapper.writeValueAsString(userAuthCacheViewDTO);
        when(valueOperations.get("user_cache:email:test@example.com")).thenReturn(userJson);

        // When
        UserAuthCacheView result = userCacheServiceAdapter.getUserByEmailFromCache("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.email());
    }

    @Test
    @DisplayName("Deve remover usuário do cache por email")
    void shouldRemoveUserFromCacheByEmail() {
        // When
        userCacheServiceAdapter.removeUserFromCacheByEmail("test@example.com");

        // Then
        verify(redisTemplate).delete("user_cache:email:test@example.com");
    }

    // By CodeUser Tests
    @Test
    @DisplayName("Deve cachear usuário por codeUser com sucesso")
    void shouldCacheUserByCodeUserSuccessfully() {
        // When
        userCacheServiceAdapter.cacheUserByCodeUser("user123", userGetByCodeView);

        // Then
        verify(valueOperations).set(eq("user_cache:codeuser:user123"), anyString(), eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve recuperar usuário por codeUser do cache")
    void shouldGetUserByCodeUserFromCache() throws Exception {
        // Given
        String userJson = objectMapper.writeValueAsString(userAuthCacheViewDTO);
        when(valueOperations.get("user_cache:codeuser:user123")).thenReturn(userJson);

        // When
        UserAuthCacheView result = userCacheServiceAdapter.getUserByCodeUserFromCache("user123");

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Deve remover usuário do cache por codeUser")
    void shouldRemoveUserFromCacheByCodeUser() {
        // When
        userCacheServiceAdapter.removeUserFromCacheByCodeUser("user123");

        // Then
        verify(redisTemplate).delete("user_cache:codeuser:user123");
    }

    // By IdExternal Tests
    @Test
    @DisplayName("Deve cachear usuário por idExternal com sucesso")
    void shouldCacheUserByIdExternalSuccessfully() {
        // When
        userCacheServiceAdapter.cacheUserByIdExternal("ext123", userAuthCacheViewDTO);

        // Then
        verify(valueOperations).set(eq("user_cache:external:ext123"), anyString(), eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve recuperar usuário por idExternal do cache")
    void shouldGetUserByIdExternalFromCache() throws Exception {
        // Given
        String userJson = objectMapper.writeValueAsString(userAuthCacheViewDTO);
        when(valueOperations.get("user_cache:external:ext123")).thenReturn(userJson);

        // When
        UserAuthCacheView result = userCacheServiceAdapter.getUserByIdExternalFromCache("ext123");

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Deve remover usuário do cache por idExternal")
    void shouldRemoveUserFromCacheByIdExternal() {
        // When
        userCacheServiceAdapter.removeUserFromCacheByIdExternal("ext123");

        // Then
        verify(redisTemplate).delete("user_cache:external:ext123");
    }

    // User Roles Tests
    @Test
    @DisplayName("Deve cachear roles do usuário com sucesso")
    void shouldCacheUserRolesSuccessfully() {
        // When
        userCacheServiceAdapter.cacheUserRoles("user123", userRolesCacheViewDTO);

        // Then
        verify(valueOperations).set(eq("user_roles_cache:user123"), anyString(), eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve recuperar roles do usuário do cache")
    void shouldGetUserRolesFromCache() throws Exception {
        // Given
        String rolesJson = objectMapper.writeValueAsString(userRolesCacheViewDTO);
        when(valueOperations.get("user_roles_cache:user123")).thenReturn(rolesJson);

        // When
        UserRolesCacheView result = userCacheServiceAdapter.getUserRolesFromCache("user123");

        // Then
        assertNotNull(result);
        assertEquals(2, result.roles().size());
        assertTrue(result.roles().contains("ADMIN"));
        assertTrue(result.roles().contains("USER"));
    }

    @Test
    @DisplayName("Deve remover roles do usuário do cache")
    void shouldRemoveUserRolesFromCache() {
        // When
        userCacheServiceAdapter.removeUserRolesFromCache("user123");

        // Then
        verify(redisTemplate).delete("user_roles_cache:user123");
    }

    // Null Tests
    @Test
    @DisplayName("Deve retornar null quando usuário não existe no cache por username")
    void shouldReturnNullWhenUserDoesNotExistInCacheByUsername() {
        // Given
        when(valueOperations.get("user_cache:username:nonexistent")).thenReturn(null);

        // When
        UserAuthCacheView result = userCacheServiceAdapter.getUserByUsernameFromCache("nonexistent");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null quando roles não existem no cache")
    void shouldReturnNullWhenRolesDoNotExistInCache() {
        // Given
        when(valueOperations.get("user_roles_cache:user123")).thenReturn(null);

        // When
        UserRolesCacheView result = userCacheServiceAdapter.getUserRolesFromCache("user123");

        // Then
        assertNull(result);
    }

}
