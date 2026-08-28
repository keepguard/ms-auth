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
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        username = "testuser";
        email = "test@example.com";
        idUserExternal = UUID.randomUUID();
        codeUser = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        user = UserTestBuilder.aUser()
            .withUsername(username)
            .withEmail(email)
            .withIdUserExternal(idUserExternal)
            .withCodeUser(codeUser)
            .withCompanyId(companyId)
            .asActive()
            .buildDomain();
    }
    
    @Test
    @DisplayName("Deve encontrar usuário por username")
    void shouldFindUserByUsername() {
        // Given
        when(userRepository.findByUsernameAndCompanyId(username, companyId)).thenReturn(Optional.of(user));
        
        // When
        Optional<User> result = authQueryService.findByUsername(username, companyId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, companyId);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por username")
    void shouldReturnEmptyOptionalWhenUserNotFoundByUsername() {
        // Given
        when(userRepository.findByUsernameAndCompanyId(username, companyId)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = authQueryService.findByUsername(username, companyId);
        
        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, companyId);
    }
    
    @Test
    @DisplayName("Deve encontrar usuário por email")
    void shouldFindUserByEmail() {
        // Given
        when(userRepository.findByEmailAndCompanyId(email, companyId)).thenReturn(Optional.of(user));
        
        // When
        Optional<User> result = authQueryService.findByEmail(email, companyId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findByEmailAndCompanyId(email, companyId);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por email")
    void shouldReturnEmptyOptionalWhenUserNotFoundByEmail() {
        // Given
        when(userRepository.findByEmailAndCompanyId(email, companyId)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = authQueryService.findByEmail(email, companyId);
        
        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmailAndCompanyId(email, companyId);
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
        when(userRepository.findByUsernameAndCompanyId(specificUsername, companyId)).thenReturn(Optional.of(user));
        
        // When
        authQueryService.findByUsername(specificUsername, companyId);
        
        // Then
        verify(userRepository, times(1)).findByUsernameAndCompanyId(specificUsername, companyId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).findByIdUserExternal(any());
        verify(userRepository, never()).findByCodeUser(any());
    }
    
    @Test
    @DisplayName("Deve chamar repository com parâmetros corretos para findByEmail")
    void shouldCallRepositoryWithCorrectParametersForFindByEmail() {
        // Given
        String specificEmail = "specific@example.com";
        when(userRepository.findByEmailAndCompanyId(specificEmail, companyId)).thenReturn(Optional.of(user));
        
        // When
        authQueryService.findByEmail(specificEmail, companyId);
        
        // Then
        verify(userRepository, times(1)).findByEmailAndCompanyId(specificEmail, companyId);
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
        when(userRepository.findByUsernameAndCompanyId(username, companyId)).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authQueryService.findByUsername(username, companyId);
        });
        
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, companyId);
    }
}
