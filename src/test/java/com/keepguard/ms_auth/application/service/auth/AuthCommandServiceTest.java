package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.lib_common.communication.enums.MessageTypeEnum;
import com.keepguard.lib_common.communication.enums.TemplateTypeEnum;
import com.keepguard.ms_auth.application.port.out.persistence.DeviceBlacklistRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserDeviceRepositoryPort;
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
import com.keepguard.ms_auth.adapters.out.feign.CompanyClient;
import com.keepguard.ms_auth.adapters.out.feign.UserClient;
import com.keepguard.ms_auth.application.port.out.cache.SessionCachePort;
import com.keepguard.ms_auth.application.service.session.DeviceSessionService;
import com.keepguard.ms_auth.infrastructure.config.security.LoginAttemptService;
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
    private UserDeviceRepositoryPort userDeviceRepository;
    
    @Mock
    private DeviceBlacklistRepositoryPort deviceBlacklistRepository;
    
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
    
    @Mock
    private CompanyClient companyClient;

    @Mock
    private SessionCachePort sessionCachePort;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private DeviceSessionService deviceSessionService;
    
    @InjectMocks
    private AuthCommandService authCommandService;
    
    private User user;
    private String username;
    private String password;
    private String token;
    private UUID codeUser;
    private UUID tenantId;
    
    @BeforeEach
    void setUp() {
        username = "testuser";
        password = "password123";
        token = "jwt-token";
        codeUser = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        
        user = UserTestBuilder.aUser()
            .withUsername(username)
            .withCodeUser(codeUser)
            .withTenantId(tenantId)
            .asActive()
            .buildDomain();
    }
    
    @Test
    @DisplayName("Deve realizar login com sucesso")
    void shouldLoginSuccessfully() {
        // Given
        when(userRepository.findByUsernameAndCompanyId(username, user.getCompanyId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(List.of());
        when(jwtService.generateToken(any(), any(), any(), anyString(), any(), any())).thenReturn(token);
        when(jwtService.getExpiration()).thenReturn(3600L);
        when(sessionCachePort.getUserSession(anyString(), anyString())).thenReturn(Optional.of(
                com.keepguard.ms_auth.domain.entity.session.UserSession.builder()
                        .isTrusted(true)
                        .createdAt("2026-08-24T10:00:00")
                        .build()
        ));
        
        // When
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .deviceId("dev_test_123")
            .build();
        var result = authCommandService.login(loginRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(token, result.token());
        assertEquals("AUTHENTICATED", result.status());
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, user.getCompanyId());
        verify(passwordEncoder, times(1)).matches(password, user.getPasswordHash());
        verify(userRoleRepository, times(2)).findByUserId(user.getId()); // Chamado 2x: getUserRoles e getUserAuthorities
        verify(jwtService, times(1)).generateToken(any(), any(), any(), anyString(), any(), any());
        verify(userRepository, times(1)).save(user);
        verify(tokenCachePort, times(1)).saveToken(codeUser.toString(), token, 3600L);
        verify(sessionCachePort, times(1)).saveUserSession(any(), eq(2592000L));
        verify(metricsPort, times(1)).incrementCounter(anyString(), any());
        verify(loginAttemptService, times(1)).recordSuccessfulAttempt(username);
    }

    @Test
    @DisplayName("Deve rejeitar login quando dispositivo está na blacklist")
    void shouldRejectLoginWhenDeviceIsBlacklisted() {
        // Given
        when(userRepository.findByUsernameAndCompanyId(username, user.getCompanyId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(sessionCachePort.isDeviceBlacklisted(codeUser.toString(), "dev_blacklisted")).thenReturn(true);

        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .deviceId("dev_blacklisted")
            .build();

        assertThrows(com.keepguard.ms_auth.application.service.exception.DeviceBlacklistedException.class, () -> {
            authCommandService.login(loginRequest);
        });

        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a conta está bloqueada")
    void shouldThrowExceptionWhenAccountIsLocked() {
        // Given
        when(loginAttemptService.isAccountLocked(username)).thenReturn(true);
        when(loginAttemptService.getRemainingLockoutTime(username)).thenReturn(15L);

        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .build();
        
        com.keepguard.ms_auth.application.service.exception.AccountLockedException exception =
            assertThrows(com.keepguard.ms_auth.application.service.exception.AccountLockedException.class, () -> {
                authCommandService.login(loginRequest);
            });

        assertTrue(exception.getMessage().contains("Conta temporariamente bloqueada"));
        verify(userRepository, never()).findByUsernameAndCompanyId(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsernameAndCompanyId(username, user.getCompanyId())).thenReturn(Optional.empty());
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .build();
        assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, user.getCompanyId());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando senha incorreta")
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // Given
        when(userRepository.findByUsernameAndCompanyId(username, user.getCompanyId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(false);
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .build();
        assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, user.getCompanyId());
        verify(passwordEncoder, times(1)).matches(password, user.getPasswordHash());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não está ativo")
    void shouldThrowExceptionWhenUserNotActive() {
        // Given
        User blockedUser = UserTestBuilder.aUser()
            .withUsername(username)
            .withTenantId(tenantId)
            .asBlocked()
            .buildDomain();
        
        when(userRepository.findByUsernameAndCompanyId(username, user.getCompanyId())).thenReturn(Optional.of(blockedUser));
        when(passwordEncoder.matches(password, blockedUser.getPasswordHash())).thenReturn(true);
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .build();
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        assertEquals("User is not active", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, user.getCompanyId());
        verify(passwordEncoder, times(1)).matches(password, blockedUser.getPasswordHash());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando email não verificado")
    void shouldThrowExceptionWhenEmailNotVerified() {
        // Given
        User unverifiedUser = UserTestBuilder.aUser()
            .withUsername(username)
            .withTenantId(tenantId)
            .withEmailVerified(false)
            .buildDomain();
        
        when(userRepository.findByUsernameAndCompanyId(username, user.getCompanyId())).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches(password, unverifiedUser.getPasswordHash())).thenReturn(true);
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .build();
        assertThrows(EmailNotVerifiedException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, user.getCompanyId());
        verify(passwordEncoder, times(1)).matches(password, unverifiedUser.getPasswordHash());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve atualizar último login do usuário")
    void shouldUpdateUserLastLogin() {
        // Given
        when(userRepository.findByUsernameAndCompanyId(username, user.getCompanyId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(List.of());
        when(jwtService.generateToken(any(), any(), any(), anyString(), any(), any())).thenReturn(token);
        when(jwtService.getExpiration()).thenReturn(3600L);
        when(sessionCachePort.getUserSession(anyString(), anyString())).thenReturn(Optional.of(
                com.keepguard.ms_auth.domain.entity.session.UserSession.builder().isTrusted(true).build()
        ));
        
        // When
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .deviceId("dev_test_123")
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
            .tenantId(tenantId)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(true);
        when(userRepository.findByCodeUserAndTenantId(codeUser, tenantId)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(List.of());
        when(jwtService.generateToken(any(), any(), any(), anyString(), any(), any())).thenReturn("new-token");
        when(jwtService.getExpiration()).thenReturn(3600L);
        
        // When
        String result = authCommandService.refreshToken(refreshRequest);
        
        // Then
        assertEquals("new-token", result);
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(userRepository, times(1)).findByCodeUserAndTenantId(codeUser, tenantId);
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
            .tenantId(tenantId)
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
    @DisplayName("Deve lançar exceção quando token já foi rotacionado (revogado no Redis)")
    void shouldThrowExceptionWhenTokenAlreadyRotated() {
        // Given
        AuthRefreshTokenCommandDTO refreshRequest = AuthRefreshTokenCommandDTO.builder()
            .token(token)
            .tenantId(tenantId)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.refreshToken(refreshRequest);
        });
        
        assertEquals("Token revogado ou sessão encerrada", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(userRepository, never()).findByCodeUserAndTenantId(any(), any());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado no refresh")
    void shouldThrowExceptionWhenUserNotFoundInRefresh() {
        // Given
        AuthRefreshTokenCommandDTO refreshRequest = AuthRefreshTokenCommandDTO.builder()
            .token(token)
            .tenantId(tenantId)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(true);
        when(userRepository.findByCodeUserAndTenantId(codeUser, tenantId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            authCommandService.refreshToken(refreshRequest);
        });
        
        assertEquals("User not found", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(userRepository, times(1)).findByCodeUserAndTenantId(codeUser, tenantId);
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não está ativo no refresh")
    void shouldThrowExceptionWhenUserNotActiveInRefresh() {
        // Given
        AuthRefreshTokenCommandDTO refreshRequest = AuthRefreshTokenCommandDTO.builder()
            .token(token)
            .tenantId(tenantId)
            .build();
        
        User blockedUser = UserTestBuilder.aUser()
            .asBlocked()
            .buildDomain();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(true);
        when(userRepository.findByCodeUserAndTenantId(codeUser, tenantId)).thenReturn(Optional.of(blockedUser));
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.refreshToken(refreshRequest);
        });
        
        assertEquals("User is not active", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(userRepository, times(1)).findByCodeUserAndTenantId(codeUser, tenantId);
        verify(jwtService, never()).generateToken(any(), any(), any(), anyString(), any(), any());
    }
    
    @Test
    @DisplayName("Deve realizar logout com sucesso")
    void shouldLogoutSuccessfully() {
        // Given
        AuthLogoutCommandDTO logoutRequest = AuthLogoutCommandDTO.builder()
            .token(token)
            .tenantId(tenantId)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(true);
        
        // When
        authCommandService.logout(logoutRequest);
        
        // Then
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(tokenCachePort, times(1)).removeAllTokens(codeUser.toString());
        verify(metricsPort, times(1)).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção no logout quando token JWT é inválido")
    void shouldThrowExceptionOnLogoutWhenTokenInvalid() {
        // Given
        AuthLogoutCommandDTO logoutRequest = AuthLogoutCommandDTO.builder()
            .token(token)
            .tenantId(tenantId)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.logout(logoutRequest);
        });
        
        assertEquals("Token inválido", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, never()).extractUserId(anyString());
        verify(tokenCachePort, never()).removeAllTokens(anyString());
        verify(metricsPort, never()).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção no logout quando sessão já está encerrada")
    void shouldThrowExceptionOnLogoutWhenSessionAlreadyTerminated() {
        // Given
        AuthLogoutCommandDTO logoutRequest = AuthLogoutCommandDTO.builder()
            .token(token)
            .tenantId(tenantId)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.logout(logoutRequest);
        });
        
        assertEquals("Sessão já encerrada", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(tokenCachePort, never()).removeAllTokens(anyString());
        verify(metricsPort, never()).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante o login")
    void shouldHandleExceptionsDuringLogin() {
        // Given
        when(userRepository.findByUsernameAndCompanyId(username, user.getCompanyId())).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        AuthLoginCommandDTO loginRequest = AuthLoginCommandDTO.builder()
            .username(username)
            .password(password)
            .tenantId(tenantId)
            .companyId(user.getCompanyId())
            .build();
        assertThrows(RuntimeException.class, () -> {
            authCommandService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndCompanyId(username, user.getCompanyId());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Deve validar token com sucesso")
    void shouldValidateTokenSuccessfully() {
        // Given
        AuthValidateTokenQueryDTO validateRequest = AuthValidateTokenQueryDTO.builder()
            .token(token)
            .tenantId(tenantId)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(userRepository.findByCodeUserAndTenantId(codeUser, tenantId)).thenReturn(Optional.of(user));
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(true);
        
        // When
        authCommandService.validateToken(validateRequest);
        
        // Then
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findByCodeUserAndTenantId(codeUser, tenantId);
        verify(tokenCachePort, times(1)).isTokenValid(codeUser.toString(), token);
        verify(metricsPort, times(1)).incrementCounter(anyString(), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando token JWT é inválido")
    void shouldThrowExceptionWhenJwtTokenIsInvalid() {
        // Given
        AuthValidateTokenQueryDTO validateRequest = AuthValidateTokenQueryDTO.builder()
            .token(token)
            .tenantId(tenantId)
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
            .tenantId(tenantId)
            .build();
        
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(codeUser);
        when(userRepository.findByCodeUserAndTenantId(codeUser, tenantId)).thenReturn(Optional.of(user));
        when(tokenCachePort.isTokenValid(codeUser.toString(), token)).thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.validateToken(validateRequest);
        });
        
        assertEquals("Token inválido ou expirado", exception.getMessage());
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findByCodeUserAndTenantId(codeUser, tenantId);
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
            .tenantId(tenantId)
            .build();
        
        when(userRepository.findByCodeUserAndTenantId(codeUserUuid, tenantId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpassword", "hashedpassword")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encoded-new-password");
        when(passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());
        
        // When
        authCommandService.changePassword(changePasswordRequest);
        
        // Then
        verify(userRepository, times(1)).findByCodeUserAndTenantId(codeUserUuid, tenantId);
        verify(passwordEncoder, times(1)).matches("oldpassword", "hashedpassword");
        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userRepository, times(1)).save(user);
        verify(passwordHistoryRepository, times(1)).save(any());
        verify(metricsPort, times(1)).incrementCounter(anyString(), any());
        verify(deviceSessionService, times(1)).notifyPasswordChanged(any());
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
            .tenantId(tenantId)
            .build();
        
        // When & Then
        assertThrows(Exception.class, () -> {
            authCommandService.changePassword(changePasswordRequest);
        });
        
        verify(userRepository, never()).findByCodeUserAndTenantId(any(), any());
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
            .tenantId(tenantId)
            .build();
        
        when(userRepository.findByCodeUserAndTenantId(codeUserUuid, tenantId)).thenReturn(Optional.of(user));
        when(tokenCachePort.isResetTokenValid(codeUserString, "EMAIL", "RECUPERACAO_SENHA", "reset-token")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encoded-new-password");
        when(passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());
        
        // When
        authCommandService.resetPassword(resetPasswordRequest);
        
        // Then
        verify(userRepository, times(1)).findByCodeUserAndTenantId(codeUserUuid, tenantId);
        verify(tokenCachePort, times(1)).isResetTokenValid(codeUserString, "EMAIL", "RECUPERACAO_SENHA", "reset-token");
        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userRepository, times(1)).save(user);
        verify(passwordHistoryRepository, times(1)).save(any());
        verify(tokenCachePort, times(1)).removeResetToken(codeUserString, "EMAIL", "RECUPERACAO_SENHA");
        verify(tokenCachePort, times(1)).clearResetTokenAttempts(codeUserString, "EMAIL", "RECUPERACAO_SENHA");
        verify(deviceSessionService, times(1)).notifyPasswordChanged(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando cooldown para geração de reset token está ativo")
    void shouldThrowExceptionWhenResetTokenCooldownIsActive() {
        // Given
        String codeUserString = codeUser.toString();
        com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenCommandDTO request =
            com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenCommandDTO.builder()
                .codeUser(codeUserString)
                .tenantId(tenantId)
                .messageType(MessageTypeEnum.EMAIL)
                .templateType(TemplateTypeEnum.RECUPERACAO_SENHA)
                .build();

        when(tokenCachePort.isResetTokenCooldownActive(codeUserString)).thenReturn(true);
        when(tokenCachePort.getResetTokenCooldownRemaining(codeUserString)).thenReturn(45L);

        // When & Then
        com.keepguard.ms_auth.application.service.exception.ResetTokenCooldownException exception =
            assertThrows(com.keepguard.ms_auth.application.service.exception.ResetTokenCooldownException.class, () -> {
                authCommandService.generateResetToken(request);
            });

        assertTrue(exception.getMessage().contains("Aguarde 45 segundos"));
        verify(userRepository, never()).findByCodeUserAndTenantId(any(), any());
    }

    @Test
    @DisplayName("Deve registrar tentativa incorreta de token de reset quando token inválido")
    void shouldRecordFailedAttemptWhenResetTokenIsInvalid() {
        // Given
        String codeUserString = codeUser.toString();
        UUID codeUserUuid = codeUser;
        AuthResetPasswordCommandDTO resetPasswordRequest = AuthResetPasswordCommandDTO.builder()
            .codeUser(codeUserString)
            .resetToken("wrong-token")
            .newPassword("newpassword123")
            .confirmNewPassword("newpassword123")
            .messageType(MessageTypeEnum.EMAIL)
            .templateType(TemplateTypeEnum.RECUPERACAO_SENHA)
            .tenantId(tenantId)
            .build();

        when(userRepository.findByCodeUserAndTenantId(codeUserUuid, tenantId)).thenReturn(Optional.of(user));
        when(tokenCachePort.isResetTokenValid(codeUserString, "EMAIL", "RECUPERACAO_SENHA", "wrong-token")).thenReturn(false);
        when(tokenCachePort.recordResetTokenFailedAttempt(codeUserString, "EMAIL", "RECUPERACAO_SENHA")).thenReturn(1L);

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authCommandService.resetPassword(resetPasswordRequest);
        });

        verify(tokenCachePort, times(1)).recordResetTokenFailedAttempt(codeUserString, "EMAIL", "RECUPERACAO_SENHA");
        verify(userRepository, never()).save(any());
    }
}
