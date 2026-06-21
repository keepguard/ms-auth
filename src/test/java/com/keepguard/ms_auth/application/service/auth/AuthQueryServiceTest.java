package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AuthQueryService
 */
@ExtendWith(MockitoExtension.class)
class AuthQueryServiceTest {
    
    @Mock
    private UserRepositoryPort userRepository;
    
    @InjectMocks
    private AuthQueryService authQueryService;
    
    private User user;
    private String username;
    private String email;
    private UUID idUserExternal;
    private UUID codeUser;
    
    @BeforeEach
    void setUp() {
        username = "testuser";
        email = "test@example.com";
        idUserExternal = UUID.randomUUID();
        codeUser = UUID.randomUUID();
        
        user = UserTestBuilder.aUser()
            .withUsername(username)
            .withEmail(email)
            .withIdUserExternal(idUserExternal)
            .withCodeUser(codeUser)
            .asActive()
            .buildDomain();
    }
    
    @Test
    @DisplayName("Deve encontrar usuário por username")
    void shouldFindUserByUsername() {
        // Given
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        
        // When
        Optional<User> result = authQueryService.findByUsername(username);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findByUsername(username);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por username")
    void shouldReturnEmptyOptionalWhenUserNotFoundByUsername() {
        // Given
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = authQueryService.findByUsername(username);
        
        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByUsername(username);
    }
    
    @Test
    @DisplayName("Deve encontrar usuário por email")
    void shouldFindUserByEmail() {
        // Given
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        // When
        Optional<User> result = authQueryService.findByEmail(email);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findByEmail(email);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por email")
    void shouldReturnEmptyOptionalWhenUserNotFoundByEmail() {
        // Given
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = authQueryService.findByEmail(email);
        
        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail(email);
    }
    
    @Test
    @DisplayName("Deve encontrar usuário por ID externo")
    void shouldFindUserByIdUserExternal() {
        // Given
        when(userRepository.findByIdUserExternal(idUserExternal)).thenReturn(Optional.of(user));
        
        // When
        Optional<User> result = authQueryService.findByIdUserExternal(idUserExternal);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findByIdUserExternal(idUserExternal);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por ID externo")
    void shouldReturnEmptyOptionalWhenUserNotFoundByIdUserExternal() {
        // Given
        when(userRepository.findByIdUserExternal(idUserExternal)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = authQueryService.findByIdUserExternal(idUserExternal);
        
        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByIdUserExternal(idUserExternal);
    }
    
    @Test
    @DisplayName("Deve encontrar usuário por código de usuário")
    void shouldFindUserByCodeUser() {
        // Given
        when(userRepository.findByCodeUser(codeUser)).thenReturn(Optional.of(user));
        
        // When
        Optional<User> result = authQueryService.findByCodeUser(codeUser);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findByCodeUser(codeUser);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por código de usuário")
    void shouldReturnEmptyOptionalWhenUserNotFoundByCodeUser() {
        // Given
        when(userRepository.findByCodeUser(codeUser)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = authQueryService.findByCodeUser(codeUser);
        
        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByCodeUser(codeUser);
    }
    
    @Test
    @DisplayName("Deve chamar repository com parâmetros corretos para findByUsername")
    void shouldCallRepositoryWithCorrectParametersForFindByUsername() {
        // Given
        String specificUsername = "specificuser";
        when(userRepository.findByUsername(specificUsername)).thenReturn(Optional.of(user));
        
        // When
        authQueryService.findByUsername(specificUsername);
        
        // Then
        verify(userRepository, times(1)).findByUsername(specificUsername);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).findByIdUserExternal(any());
        verify(userRepository, never()).findByCodeUser(any());
    }
    
    @Test
    @DisplayName("Deve chamar repository com parâmetros corretos para findByEmail")
    void shouldCallRepositoryWithCorrectParametersForFindByEmail() {
        // Given
        String specificEmail = "specific@example.com";
        when(userRepository.findByEmail(specificEmail)).thenReturn(Optional.of(user));
        
        // When
        authQueryService.findByEmail(specificEmail);
        
        // Then
        verify(userRepository, times(1)).findByEmail(specificEmail);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByIdUserExternal(any());
        verify(userRepository, never()).findByCodeUser(any());
    }
    
    @Test
    @DisplayName("Deve chamar repository com parâmetros corretos para findByIdUserExternal")
    void shouldCallRepositoryWithCorrectParametersForFindByIdUserExternal() {
        // Given
        UUID specificIdUserExternal = UUID.randomUUID();
        when(userRepository.findByIdUserExternal(specificIdUserExternal)).thenReturn(Optional.of(user));
        
        // When
        authQueryService.findByIdUserExternal(specificIdUserExternal);
        
        // Then
        verify(userRepository, times(1)).findByIdUserExternal(specificIdUserExternal);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).findByCodeUser(any());
    }
    
    @Test
    @DisplayName("Deve chamar repository com parâmetros corretos para findByCodeUser")
    void shouldCallRepositoryWithCorrectParametersForFindByCodeUser() {
        // Given
        UUID specificCodeUser = UUID.randomUUID();
        when(userRepository.findByCodeUser(specificCodeUser)).thenReturn(Optional.of(user));
        
        // When
        authQueryService.findByCodeUser(specificCodeUser);
        
        // Then
        verify(userRepository, times(1)).findByCodeUser(specificCodeUser);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).findByIdUserExternal(any());
    }
    
    @Test
    @DisplayName("Deve lidar com exceções do repository")
    void shouldHandleRepositoryExceptions() {
        // Given
        when(userRepository.findByUsername(username)).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authQueryService.findByUsername(username);
        });
        
        verify(userRepository, times(1)).findByUsername(username);
    }
}
