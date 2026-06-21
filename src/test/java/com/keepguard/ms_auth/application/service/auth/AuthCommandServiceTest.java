package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.lib_common.communication.enums.MessageTypeEnum;
import com.keepguard.lib_common.communication.enums.TemplateTypeEnum;
import com.keepguard.ms_auth.application.port.out.persistence.PasswordHistoryRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.EmailNotVerifiedException;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import com.keepguard.ms_auth.domain.dto.auth.AuthLoginCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthRefreshTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthLogoutCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthValidateTokenQueryDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthChangePasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthResetPasswordCommandDTO;
import com.keepguard.ms_auth.adapters.out.feign.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AuthCommandService
 */
@ExtendWith(MockitoExtension.class)
class AuthCommandServiceTest {
    
    @Mock
    private UserRepositoryPort userRepository;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private TokenCachePort tokenCachePort;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private PasswordHistoryRepositoryPort passwordHistoryRepository;
    
    @Mock
    private MetricsPort metricsPort;
    
    @Mock
    private UserRoleRepositoryPort userRoleRepository;
    
    @Mock
    private RoleRepositoryPort roleRepository;
    
    @Mock
    private UserClient userClient;
    
    @InjectMocks
    private AuthCommandService authCommandService;
    
    private User user;
    private String username;
    private String password;
    private String token;
    private UUID codeUser;
    private UUID xApplicationUuid;
    
    @BeforeEach
    void setUp() {
        username = "testuser";
        password = "password123";
        token = "jwt-token";
        codeUser = UUID.randomUUID();
        xApplicationUuid = UUID.randomUUID();
        
        user = UserTestBuilder.aUser()
            .withUsername(username)
            .withCodeUser(codeUser)
            .withXApplication(xApplicationUuid)
            .asActive()
            .buildDomain();
    }
    
    @Test
    @DisplayName("Deve realizar login com sucesso")
    void shouldLoginSuccessfully() {
        // Given
        when(userRepository.findByUsernameAndXApplication(username, xApplicationUuid)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(List.of());
        when(jwtService.generateToken(any(), any(), any(), anyString(), any(), any())).thenReturn(token);
        when(jwtService.getExpiration()).thenReturn(3600L);
        
        // When
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .xApplicationUuid(xApplicationUuid)
            .build();
        String result = authCommandService.login(loginRequest);
        
        // Then
        assertEquals(token, result);
        verify(userRepository, times(1)).findByUsernameAndXApplication(username, xApplicationUuid);
        verify(passwordEncoder, times(1)).matches(password, user.getPasswordHash());
        verify(userRoleRepository, times(2)).findByUserId(user.getId()); // Chamado 2x: getUserRoles e getUserAuthorities
        verify(jwtService, times(1)).generateToken(any(), any(), any(), anyString(), any(), any());
        verify(userRepository, times(1)).save(user);
        verify(tokenCachePort, times(1)).saveToken(codeUser.toString(), token, 3600L);
        verify(metricsPort, times(1)).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsernameAndXApplication(username, xApplicationUuid)).thenReturn(Optional.empty());
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .xApplicationUuid(xApplicationUuid)
            .build();
        assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndXApplication(username, xApplicationUuid);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando senha incorreta")
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // Given
        when(userRepository.findByUsernameAndXApplication(username, xApplicationUuid)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(false);
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .xApplicationUuid(xApplicationUuid)
            .build();
        assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndXApplication(username, xApplicationUuid);
        verify(passwordEncoder, times(1)).matches(password, user.getPasswordHash());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não está ativo")
    void shouldThrowExceptionWhenUserNotActive() {
        // Given
        User blockedUser = UserTestBuilder.aUser()
            .withUsername(username)
            .withXApplication(xApplicationUuid)
            .asBlocked()
            .buildDomain();
        
        when(userRepository.findByUsernameAndXApplication(username, xApplicationUuid)).thenReturn(Optional.of(blockedUser));
        when(passwordEncoder.matches(password, blockedUser.getPasswordHash())).thenReturn(true);
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .xApplicationUuid(xApplicationUuid)
            .build();
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        assertEquals("User is not active", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameAndXApplication(username, xApplicationUuid);
        verify(passwordEncoder, times(1)).matches(password, blockedUser.getPasswordHash());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando email não verificado")
    void shouldThrowExceptionWhenEmailNotVerified() {
        // Given
        User unverifiedUser = UserTestBuilder.aUser()
            .withUsername(username)
            .withXApplication(xApplicationUuid)
            .withEmailVerified(false)
            .buildDomain();
        
        when(userRepository.findByUsernameAndXApplication(username, xApplicationUuid)).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches(password, unverifiedUser.getPasswordHash())).thenReturn(true);
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .xApplicationUuid(xApplicationUuid)
            .build();
        assertThrows(EmailNotVerifiedException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndXApplication(username, xApplicationUuid);
        verify(passwordEncoder, times(1)).matches(password, unverifiedUser.getPasswordHash());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve atualizar último login do usuário")
    void shouldUpdateUserLastLogin() {
        // Given
        when(userRepository.findByUsernameAndXApplication(username, xApplicationUuid)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(List.of());
        when(jwtService.generateToken(any(), any(), any(), anyString(), any(), any())).thenReturn(token);
        when(jwtService.getExpiration()).thenReturn(3600L);
        
        // When
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .xApplicationUuid(xApplicationUuid)
            .build();
        authCommandService.login(loginRequest);
        
        // Then
        verify(userRepository, times(1)).save(user);
        assertNotNull(user.getLastLogin());
    }
    
    @Test
    @DisplayName("Deve renovar token com sucesso")
    void shouldRefreshTokenSuccessfully() {
        // Given
        AuthRefreshTokenCommandDTO refreshRequest = AuthRefreshTokenCommandDTO.builder()
            .token(token)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(userRepository.findByCodeUserAndXApplication(codeUser, xApplicationUuid)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(List.of());
        when(jwtService.generateToken(any(), any(), any(), anyString(), any(), any())).thenReturn("new-token");
        when(jwtService.getExpiration()).thenReturn(3600L);
        
        // When
        String result = authCommandService.refreshToken(refreshRequest);
        
        // Then
        assertEquals("new-token", result);
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findByCodeUserAndXApplication(codeUser, xApplicationUuid);
        verify(jwtService, times(1)).generateToken(any(), any(), any(), anyString(), any(), any());
        verify(tokenCachePort, times(1)).removeToken(codeUser.toString(), token);
        verify(tokenCachePort, times(1)).saveToken(codeUser.toString(), "new-token", 3600L);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando token inválido")
    void shouldThrowExceptionWhenTokenInvalid() {
        // Given
        AuthRefreshTokenCommandDTO refreshRequest = AuthRefreshTokenCommandDTO.builder()
            .token(token)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.refreshToken(refreshRequest);
        });
        
        assertEquals("Invalid token", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, never()).extractUserId(anyString());
        verify(userRepository, never()).findByCodeUser(any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado no refresh")
    void shouldThrowExceptionWhenUserNotFoundInRefresh() {
        // Given
        AuthRefreshTokenCommandDTO refreshRequest = AuthRefreshTokenCommandDTO.builder()
            .token(token)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(userRepository.findByCodeUserAndXApplication(codeUser, xApplicationUuid)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            authCommandService.refreshToken(refreshRequest);
        });
        
        assertEquals("User not found", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findByCodeUserAndXApplication(codeUser, xApplicationUuid);
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não está ativo no refresh")
    void shouldThrowExceptionWhenUserNotActiveInRefresh() {
        // Given
        AuthRefreshTokenCommandDTO refreshRequest = AuthRefreshTokenCommandDTO.builder()
            .token(token)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        User blockedUser = UserTestBuilder.aUser()
            .asBlocked()
            .buildDomain();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(userRepository.findByCodeUserAndXApplication(codeUser, xApplicationUuid)).thenReturn(Optional.of(blockedUser));
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.refreshToken(refreshRequest);
        });
        
        assertEquals("User is not active", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findByCodeUserAndXApplication(codeUser, xApplicationUuid);
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve realizar logout com sucesso")
    void shouldLogoutSuccessfully() {
        // Given
        AuthLogoutCommandDTO logoutRequest = AuthLogoutCommandDTO.builder()
            .token(token)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        
        // When
        authCommandService.logout(logoutRequest);
        
        // Then
        verify(jwtService, times(1)).extractUserId(token);
        verify(tokenCachePort, times(1)).removeAllTokens(codeUser.toString());
        verify(metricsPort, times(1)).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante o login")
    void shouldHandleExceptionsDuringLogin() {
        // Given
        when(userRepository.findByUsernameAndXApplication(username, xApplicationUuid)).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .xApplicationUuid(xApplicationUuid)
            .build();
        assertThrows(RuntimeException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndXApplication(username, xApplicationUuid);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Deve validar token com sucesso")
    void shouldValidateTokenSuccessfully() {
        // Given
        AuthValidateTokenQueryDTO validateRequest = AuthValidateTokenQueryDTO.builder()
            .token(token)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(userRepository.findByCodeUserAndXApplication(codeUser, xApplicationUuid)).thenReturn(Optional.of(user));
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(true);
        
        // When
        authCommandService.validateToken(validateRequest);
        
        // Then
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findByCodeUserAndXApplication(codeUser, xApplicationUuid);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(metricsPort, times(1)).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando token JWT é inválido")
    void shouldThrowExceptionWhenJwtTokenIsInvalid() {
        // Given
        AuthValidateTokenQueryDTO validateRequest = AuthValidateTokenQueryDTO.builder()
            .token(token)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.validateToken(validateRequest);
        });
        
        assertEquals("Token inválido ou expirado", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, never()).extractUserId(anyString());
        verify(tokenCachePort, never()).isTokenValid(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando token não está válido no Redis")
    void shouldThrowExceptionWhenTokenNotValidInRedis() {
        // Given
        AuthValidateTokenQueryDTO validateRequest = AuthValidateTokenQueryDTO.builder()
            .token(token)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(userRepository.findByCodeUserAndXApplication(codeUser, xApplicationUuid)).thenReturn(Optional.of(user));
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.validateToken(validateRequest);
        });
        
        assertEquals("Token inválido ou expirado", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findByCodeUserAndXApplication(codeUser, xApplicationUuid);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(metricsPort, never()).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve alterar senha com sucesso")
    void shouldChangePasswordSuccessfully() {
        // Given
        String codeUserString = codeUser.toString();
        UUID codeUserUuid = codeUser;
        AuthChangePasswordCommandDTO changePasswordRequest = AuthChangePasswordCommandDTO.builder()
            .codeUser(codeUserString)
            .currentPassword("oldpassword")
            .newPassword("newpassword123")
            .confirmNewPassword("newpassword123")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(userRepository.findByCodeUserAndXApplication(codeUserUuid, xApplicationUuid)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpassword", "hashedpassword")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encoded-new-password");
        when(passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());
        
        // When
        authCommandService.changePassword(changePasswordRequest);
        
        // Then
        verify(userRepository, times(1)).findByCodeUserAndXApplication(codeUserUuid, xApplicationUuid);
        verify(passwordEncoder, times(1)).matches("oldpassword", "hashedpassword");
        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userRepository, times(1)).save(user);
        verify(passwordHistoryRepository, times(1)).save(any());
        verify(metricsPort, times(1)).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando senhas não coincidem na alteração")
    void shouldThrowExceptionWhenPasswordsDoNotMatchInChangePassword() {
        // Given
        AuthChangePasswordCommandDTO changePasswordRequest = AuthChangePasswordCommandDTO.builder()
            .codeUser("code-123")
            .currentPassword("oldpassword")
            .newPassword("newpassword123")
            .confirmNewPassword("differentpassword")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        // When & Then
        assertThrows(Exception.class, () -> {
            authCommandService.changePassword(changePasswordRequest);
        });
        
        verify(userRepository, never()).findByCodeUserAndXApplication(any(), any());
    }
    
    @Test
    @DisplayName("Deve resetar senha com sucesso")
    void shouldResetPasswordSuccessfully() {
        // Given
        String codeUserString = codeUser.toString();
        UUID codeUserUuid = codeUser;
        AuthResetPasswordCommandDTO resetPasswordRequest = AuthResetPasswordCommandDTO.builder()
            .codeUser(codeUserString)
            .resetToken("reset-token")
            .newPassword("newpassword123")
            .confirmNewPassword("newpassword123")
            .messageType(MessageTypeEnum.EMAIL)
            .templateType(TemplateTypeEnum.RECUPERACAO_SENHA)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        when(userRepository.findByCodeUserAndXApplication(codeUserUuid, xApplicationUuid)).thenReturn(Optional.of(user));
        when(tokenCachePort.isResetTokenValid(codeUserString, "EMAIL", "RECUPERACAO_SENHA", "reset-token")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encoded-new-password");
        when(passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());
        
        // When
        authCommandService.resetPassword(resetPasswordRequest);
        
        // Then
        verify(userRepository, times(1)).findByCodeUserAndXApplication(codeUserUuid, xApplicationUuid);
        verify(tokenCachePort, times(1)).isResetTokenValid(codeUserString, "EMAIL", "RECUPERACAO_SENHA", "reset-token");
        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userRepository, times(1)).save(user);
        verify(passwordHistoryRepository, times(1)).save(any());
        verify(tokenCachePort, times(1)).removeResetToken(codeUserString, "EMAIL", "RECUPERACAO_SENHA");
    }
}
