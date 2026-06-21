package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.auth.AuthLogoutView;
import com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenView;
import com.keepguard.ms_auth.application.dto.user.UserView;
import com.keepguard.ms_auth.application.mapper.AuthApplicationMapper;
import com.keepguard.ms_auth.domain.dto.auth.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AuthUseCaseService
 */
@ExtendWith(MockitoExtension.class)
class AuthUseCaseServiceTest {
    
    @Mock
    private AuthCommandService authCommandService;
    
    @Mock
    private AuthQueryService authQueryService;
    
    @Mock
    private AuthApplicationMapper authApplicationMapper;
    
    @InjectMocks
    private AuthUseCaseService authUseCaseService;
    
    private AuthLoginCommandDTO loginRequest;
    private AuthRefreshTokenCommandDTO refreshTokenRequest;
    private AuthChangePasswordCommandDTO changePasswordRequest;
    private AuthResetPasswordCommandDTO resetPasswordRequest;
    private AuthValidateTokenQueryDTO validateTokenRequest;
    private User user;
    private UserView userView;
    private String token;
    
    @BeforeEach
    void setUp() {
        token = "jwt-token";
        
        loginRequest = AuthLoginCommandDTO.builder()
            .username("testuser")
            .password("password123")
            .xApplicationUuid(UUID.randomUUID())
            .userAgent("Mozilla/5.0")
            .build();
        
        refreshTokenRequest = AuthRefreshTokenCommandDTO.builder()
            .token(token)
            .xApplicationUuid(UUID.randomUUID())
            .userAgent("Mozilla/5.0")
            .build();
        
        changePasswordRequest = AuthChangePasswordCommandDTO.builder()
            .codeUser("code-123")
            .currentPassword("oldpassword")
            .newPassword("newpassword123")
            .confirmNewPassword("newpassword123")
            .xApplicationUuid(UUID.randomUUID())
            .build();
        
        resetPasswordRequest = AuthResetPasswordCommandDTO.builder()
            .codeUser("code-123")
            .resetToken("reset-token")
            .newPassword("newpassword123")
            .confirmNewPassword("newpassword123")
            .xApplicationUuid(UUID.randomUUID())
            .build();
        
        validateTokenRequest = AuthValidateTokenQueryDTO.builder()
            .token(token)
            .xApplicationUuid(UUID.randomUUID())
            .build();
        
        user = UserTestBuilder.aUser()
            .asActive()
            .buildDomain();
            
        userView = new UserView(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            null, // name
            user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : null,
            user.getCodeUser() != null ? user.getCodeUser().toString() : null,
            user.getStatus() != null ? user.getStatus().toString() : null,
            Boolean.TRUE.equals(user.getEmailVerified()),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLogin(),
            null, // roles
            user.getCompanyId(),
            user.getCompanyCode(),
            user.getXApplication()
        );
    }
    
    @Test
    @DisplayName("Deve realizar login com sucesso")
    void shouldLoginSuccessfully() {
        // Given
        when(authCommandService.login(loginRequest))
            .thenReturn(token);
        
        // When
        AuthLoginView response = authUseCaseService.login(loginRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(token, response.token());
        assertEquals(3600L, response.expiresIn());
        
        verify(authCommandService, times(1)).login(loginRequest);
    }
    
    @Test
    @DisplayName("Deve renovar token com sucesso")
    void shouldRefreshTokenSuccessfully() {
        // Given
        String newToken = "new-jwt-token";
        when(authCommandService.refreshToken(refreshTokenRequest))
            .thenReturn(newToken);
        
        // When
        AuthRefreshTokenView response = authUseCaseService.refreshToken(refreshTokenRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(newToken, response.token());
        assertEquals(3600L, response.expiresIn());
        
        verify(authCommandService, times(1)).refreshToken(refreshTokenRequest);
    }
    
    @Test
    @DisplayName("Deve realizar logout com sucesso")
    void shouldLogoutSuccessfully() {
        // Given
        AuthLogoutCommandDTO logoutRequest = AuthLogoutCommandDTO.builder()
            .token(token)
            .xApplicationUuid(UUID.randomUUID())
            .build();
        
        doNothing().when(authCommandService).logout(logoutRequest);
        
        // When
        AuthLogoutView response = authUseCaseService.logout(logoutRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("Logout realizado com sucesso", response.message());
        assertTrue(response.success());
        
        verify(authCommandService, times(1)).logout(logoutRequest);
    }
    
    @Test
    @DisplayName("Deve alterar senha com sucesso")
    void shouldChangePasswordSuccessfully() {
        // Given
        doNothing().when(authCommandService).changePassword(changePasswordRequest);
        
        // When
        authUseCaseService.changePassword(changePasswordRequest);
        
        // Then
        verify(authCommandService, times(1)).changePassword(changePasswordRequest);
    }
    
    @Test
    @DisplayName("Deve resetar senha com sucesso")
    void shouldResetPasswordSuccessfully() {
        // Given
        doNothing().when(authCommandService).resetPassword(resetPasswordRequest);
        
        // When
        authUseCaseService.resetPassword(resetPasswordRequest);
        
        // Then
        verify(authCommandService, times(1)).resetPassword(resetPasswordRequest);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por username com sucesso")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        String username = "testuser";
        when(authQueryService.findByUsername(username)).thenReturn(Optional.of(user));
        when(authApplicationMapper.toUserView(user)).thenReturn(userView);
        
        // When
        Optional<UserView> result = authUseCaseService.findByUsername(username);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(userView, result.get());
        
        verify(authQueryService, times(1)).findByUsername(username);
        verify(authApplicationMapper, times(1)).toUserView(user);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por username")
    void shouldReturnEmptyOptionalWhenUserNotFoundByUsername() {
        // Given
        String username = "nonexistentuser";
        when(authQueryService.findByUsername(username)).thenReturn(Optional.empty());
        
        // When
        Optional<UserView> result = authUseCaseService.findByUsername(username);
        
        // Then
        assertTrue(result.isEmpty());
        
        verify(authQueryService, times(1)).findByUsername(username);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void shouldFindUserByEmailSuccessfully() {
        // Given
        String email = "test@example.com";
        when(authQueryService.findByEmail(email)).thenReturn(Optional.of(user));
        when(authApplicationMapper.toUserView(user)).thenReturn(userView);
        
        // When
        Optional<UserView> result = authUseCaseService.findByEmail(email);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(userView, result.get());
        
        verify(authQueryService, times(1)).findByEmail(email);
        verify(authApplicationMapper, times(1)).toUserView(user);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por email")
    void shouldReturnEmptyOptionalWhenUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        when(authQueryService.findByEmail(email)).thenReturn(Optional.empty());
        
        // When
        Optional<UserView> result = authUseCaseService.findByEmail(email);
        
        // Then
        assertTrue(result.isEmpty());
        
        verify(authQueryService, times(1)).findByEmail(email);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por ID externo com sucesso")
    void shouldFindUserByIdUserExternalSuccessfully() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        when(authQueryService.findByIdUserExternal(idUserExternal)).thenReturn(Optional.of(user));
        when(authApplicationMapper.toUserView(user)).thenReturn(userView);
        
        // When
        Optional<UserView> result = authUseCaseService.findByIdUserExternal(idUserExternal);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(userView, result.get());
        
        verify(authQueryService, times(1)).findByIdUserExternal(idUserExternal);
        verify(authApplicationMapper, times(1)).toUserView(user);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por ID externo")
    void shouldReturnEmptyOptionalWhenUserNotFoundByIdUserExternal() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        when(authQueryService.findByIdUserExternal(idUserExternal)).thenReturn(Optional.empty());
        
        // When
        Optional<UserView> result = authUseCaseService.findByIdUserExternal(idUserExternal);
        
        // Then
        assertTrue(result.isEmpty());
        
        verify(authQueryService, times(1)).findByIdUserExternal(idUserExternal);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por código de usuário com sucesso")
    void shouldFindUserByCodeUserSuccessfully() {
        // Given
        UUID codeUser = UUID.randomUUID();
        when(authQueryService.findByCodeUser(codeUser)).thenReturn(Optional.of(user));
        when(authApplicationMapper.toUserView(user)).thenReturn(userView);
        
        // When
        Optional<UserView> result = authUseCaseService.findByCodeUser(codeUser);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(userView, result.get());
        
        verify(authQueryService, times(1)).findByCodeUser(codeUser);
        verify(authApplicationMapper, times(1)).toUserView(user);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por código de usuário")
    void shouldReturnEmptyOptionalWhenUserNotFoundByCodeUser() {
        // Given
        UUID codeUser = UUID.randomUUID();
        when(authQueryService.findByCodeUser(codeUser)).thenReturn(Optional.empty());
        
        // When
        Optional<UserView> result = authUseCaseService.findByCodeUser(codeUser);
        
        // Then
        assertTrue(result.isEmpty());
        
        verify(authQueryService, times(1)).findByCodeUser(codeUser);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante login")
    void shouldHandleExceptionsDuringLogin() {
        // Given
        when(authCommandService.login(any(AuthLoginCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authUseCaseService.login(loginRequest);
        });
        
        verify(authCommandService, times(1)).login(loginRequest);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante refresh token")
    void shouldHandleExceptionsDuringRefreshToken() {
        // Given
        when(authCommandService.refreshToken(any(AuthRefreshTokenCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authUseCaseService.refreshToken(refreshTokenRequest);
        });
        
        verify(authCommandService, times(1)).refreshToken(refreshTokenRequest);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante logout")
    void shouldHandleExceptionsDuringLogout() {
        // Given
        AuthLogoutCommandDTO logoutRequest = AuthLogoutCommandDTO.builder()
            .token(token)
            .xApplicationUuid(UUID.randomUUID())
            .build();
        
        doThrow(new RuntimeException("Service error")).when(authCommandService).logout(logoutRequest);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authUseCaseService.logout(logoutRequest);
        });
        
        verify(authCommandService, times(1)).logout(logoutRequest);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante change password")
    void shouldHandleExceptionsDuringChangePassword() {
        // Given
        doThrow(new RuntimeException("Service error")).when(authCommandService).changePassword(any());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authUseCaseService.changePassword(changePasswordRequest);
        });
        
        verify(authCommandService, times(1)).changePassword(changePasswordRequest);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante reset password")
    void shouldHandleExceptionsDuringResetPassword() {
        // Given
        doThrow(new RuntimeException("Service error")).when(authCommandService).resetPassword(any());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authUseCaseService.resetPassword(resetPasswordRequest);
        });
        
        verify(authCommandService, times(1)).resetPassword(resetPasswordRequest);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante busca de usuário")
    void shouldHandleExceptionsDuringUserSearch() {
        // Given
        String username = "testuser";
        when(authQueryService.findByUsername(username))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authUseCaseService.findByUsername(username);
        });
        
        verify(authQueryService, times(1)).findByUsername(username);
    }
    
    @Test
    @DisplayName("Deve validar token com sucesso")
    void shouldValidateTokenSuccessfully() {
        // Given
        doNothing().when(authCommandService).validateToken(any(AuthValidateTokenQueryDTO.class));
        
        // When
        authUseCaseService.validateToken(validateTokenRequest);
        
        // Then
        verify(authCommandService, times(1)).validateToken(validateTokenRequest);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante validação de token")
    void shouldHandleExceptionsDuringValidateToken() {
        // Given
        doThrow(new RuntimeException("Token inválido")).when(authCommandService).validateToken(any(AuthValidateTokenQueryDTO.class));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authUseCaseService.validateToken(validateTokenRequest);
        });
        
        verify(authCommandService, times(1)).validateToken(validateTokenRequest);
    }
}
