package com.keepguard.ms_auth.application.service.session;

import com.keepguard.ms_auth.adapters.out.feign.CommunicationClient;
import com.keepguard.ms_auth.adapters.out.feign.CompanyClient;
import com.keepguard.ms_auth.adapters.out.feign.UserClient;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.session.PasswordChangedNotifyCommand;
import com.keepguard.ms_auth.application.dto.session.SendDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.dto.session.VerifyDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.port.out.cache.SessionCachePort;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import com.keepguard.ms_auth.application.port.out.persistence.DeviceBlacklistRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserDeviceRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.session.DeviceChallengeSession;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceSessionServiceTest {

    @Mock
    private SessionCachePort sessionCachePort;

    @Mock
    private TokenCachePort tokenCachePort;

    @Mock
    private CommunicationClient communicationClient;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private UserRoleRepositoryPort userRoleRepository;

    @Mock
    private RoleRepositoryPort roleRepository;

    @Mock
    private UserDeviceRepositoryPort userDeviceRepository;

    @Mock
    private DeviceBlacklistRepositoryPort deviceBlacklistRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private DeviceSessionService deviceSessionService;

    private DeviceChallengeSession mockChallenge;
    private final String challengeSessionId = "chal_12345";
    private final String tenantId = UUID.randomUUID().toString();
    private final String codeUser = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        mockChallenge = DeviceChallengeSession.builder()
                .challengeSessionId(challengeSessionId)
                .codeUser(codeUser)
                .tenantId(tenantId)
                .username("testuser")
                .email("test@example.com")
                .phone("+5511999999999")
                .clientId("keepguard-default-client")
                .deviceId("device_abc")
                .deviceName("Chrome Web")
                .deviceType("DESKTOP")
                .attempts(0)
                .maxAttempts(5)
                .build();
    }

    @Test
    @DisplayName("Deve enviar desafio por EMAIL incluindo communicationType e salvar código no Redis")
    void shouldSendChallengeEmailSuccessfully() {
        when(sessionCachePort.getDeviceChallenge(challengeSessionId)).thenReturn(Optional.of(mockChallenge));

        SendDeviceChallengeCommandDTO command = SendDeviceChallengeCommandDTO.builder()
                .challengeSessionId(challengeSessionId)
                .channel("EMAIL")
                .tenantId(tenantId)
                .build();

        Map<String, Object> result = deviceSessionService.sendChallenge(command);

        assertNotNull(result);
        assertEquals("EMAIL", result.get("channel"));
        assertEquals(300, result.get("expiresIn"));

        // Verifica que o desafio foi atualizado com o código e salvo no Redis
        assertNotNull(mockChallenge.getActiveCode());
        assertEquals("EMAIL", mockChallenge.getSelectedChannel());
        verify(sessionCachePort, times(1)).saveDeviceChallenge(eq(mockChallenge), eq(600L));

        // Captura o payload enviado para o ms-communication
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(communicationClient, times(1)).sendMessage(payloadCaptor.capture(), eq(tenantId));

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertEquals("test@example.com", capturedPayload.get("recipient"));
        assertEquals(codeUser, capturedPayload.get("codeUser"));
        assertEquals("AUTENTICACAO_DISPOSITIVO_EMAIL_TOKEN", capturedPayload.get("templateType"));
        assertEquals("EMAIL", capturedPayload.get("messageType"));
        assertEquals("EMAIL", capturedPayload.get("communicationType"));
        assertNotNull(capturedPayload.get("subject"));
        assertNotNull(capturedPayload.get("variables"));

        Map<String, Object> vars = (Map<String, Object>) capturedPayload.get("variables");
        assertEquals(mockChallenge.getActiveCode(), vars.get("token"));
        assertEquals("Chrome Web", vars.get("deviceName"));
    }

    @Test
    @DisplayName("Deve enviar desafio por SMS com communicationType SMS")
    void shouldSendChallengeSmsSuccessfully() {
        when(sessionCachePort.getDeviceChallenge(challengeSessionId)).thenReturn(Optional.of(mockChallenge));

        SendDeviceChallengeCommandDTO command = SendDeviceChallengeCommandDTO.builder()
                .challengeSessionId(challengeSessionId)
                .channel("SMS")
                .tenantId(tenantId)
                .build();

        Map<String, Object> result = deviceSessionService.sendChallenge(command);

        assertNotNull(result);
        assertEquals("SMS", result.get("channel"));

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(communicationClient, times(1)).sendMessage(payloadCaptor.capture(), eq(tenantId));

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertEquals("+5511999999999", capturedPayload.get("recipient"));
        assertEquals("AUTENTICACAO_DISPOSITIVO_SMS_TOKEN", capturedPayload.get("templateType"));
        assertEquals("SMS", capturedPayload.get("messageType"));
        assertEquals("SMS", capturedPayload.get("communicationType"));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando sessão de desafio não existir")
    void shouldThrowNotFoundExceptionWhenChallengeNotFound() {
        when(sessionCachePort.getDeviceChallenge("invalid_chal")).thenReturn(Optional.empty());

        SendDeviceChallengeCommandDTO command = SendDeviceChallengeCommandDTO.builder()
                .challengeSessionId("invalid_chal")
                .channel("EMAIL")
                .build();

        assertThrows(NotFoundException.class, () -> deviceSessionService.sendChallenge(command));
        verify(communicationClient, never()).sendMessage(any(), any());
    }

    @Test
    @DisplayName("Deve validar código de desafio com sucesso e gerar token")
    void shouldVerifyChallengeSuccessfully() {
        mockChallenge.setActiveCode("123456");
        when(sessionCachePort.getDeviceChallenge(challengeSessionId)).thenReturn(Optional.of(mockChallenge));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCodeUser(UUID.fromString(codeUser));
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByCodeUserAndTenantId(UUID.fromString(codeUser), UUID.fromString(tenantId)))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("mock-jwt-token");
        when(jwtService.getExpiration()).thenReturn(3600L);
        when(userRoleRepository.findByUserId(any())).thenReturn(Collections.emptyList());

        VerifyDeviceChallengeCommandDTO command = VerifyDeviceChallengeCommandDTO.builder()
                .challengeSessionId(challengeSessionId)
                .code("123456")
                .tenantId(tenantId)
                .trustDevice(true)
                .build();

        AuthLoginView result = deviceSessionService.verifyChallenge(command);

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.token());
        assertEquals("AUTHENTICATED", result.status());

        verify(tokenCachePort, times(1)).saveToken(eq(codeUser), eq("mock-jwt-token"), eq(3600L));
        verify(sessionCachePort, times(1)).saveUserSession(any(), eq(2592000L));
        verify(sessionCachePort, times(1)).saveQuickRevokeToken(any(), eq(172800L));
        verify(sessionCachePort, times(1)).removeDeviceChallenge(challengeSessionId);
    }

    @Test
    @DisplayName("Deve processar quickRevoke com sucesso e adicionar à blacklist se solicitado")
    void shouldQuickRevokeSuccessfully() {
        String token = "quick_token_123";
        com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken qrt = com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken.builder()
                .token(token)
                .codeUser(codeUser)
                .deviceId("device_abc")
                .deviceName("Chrome Web")
                .build();

        when(sessionCachePort.getQuickRevokeToken(token)).thenReturn(Optional.of(qrt));

        Map<String, Object> response = deviceSessionService.quickRevoke(token, true);

        assertNotNull(response);
        assertEquals("device_abc", response.get("deviceId"));
        assertEquals(true, response.get("blacklisted"));

        verify(sessionCachePort, times(1)).removeUserSession(codeUser, "device_abc");
        verify(sessionCachePort, times(1)).addToBlacklist(any(), eq(0L));
        verify(sessionCachePort, times(1)).removeQuickRevokeToken(token);
    }

    @Test
    @DisplayName("Deve adicionar e remover dispositivo da blacklist")
    void shouldAddAndRemoveFromBlacklist() {
        deviceSessionService.addDeviceToBlacklist(codeUser, "device_xyz", "iPhone", "Perdido");

        verify(sessionCachePort, times(1)).removeUserSession(codeUser, "device_xyz");
        verify(sessionCachePort, times(1)).addToBlacklist(any(), eq(0L));

        deviceSessionService.removeDeviceFromBlacklist(codeUser, "device_xyz");
        verify(sessionCachePort, times(1)).removeFromBlacklist(codeUser, "device_xyz");
    }

    @Test
    @DisplayName("Deve rejeitar código inválido e decrementar tentativas")
    void shouldRejectInvalidChallengeCode() {
        mockChallenge.setActiveCode("123456");
        when(sessionCachePort.getDeviceChallenge(challengeSessionId)).thenReturn(Optional.of(mockChallenge));

        VerifyDeviceChallengeCommandDTO command = VerifyDeviceChallengeCommandDTO.builder()
                .challengeSessionId(challengeSessionId)
                .code("000000")
                .tenantId(tenantId)
                .build();

        assertThrows(InvalidCredentialsException.class, () -> deviceSessionService.verifyChallenge(command));
        assertEquals(1, mockChallenge.getAttempts());
        verify(sessionCachePort, times(1)).saveDeviceChallenge(eq(mockChallenge), eq(600L));
    }

    @Test
    @DisplayName("Deve salvar dispositivo no PostgreSQL ao verificar desafio com sucesso")
    void shouldPersistUserDeviceInPostgresOnVerifyChallengeSuccess() {
        mockChallenge.setActiveCode("123456");
        when(sessionCachePort.getDeviceChallenge(challengeSessionId)).thenReturn(Optional.of(mockChallenge));

        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .codeUser(UUID.fromString(codeUser))
                .tenantId(UUID.fromString(tenantId))
                .username("testuser")
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByCodeUserAndTenantId(UUID.fromString(codeUser), UUID.fromString(tenantId)))
                .thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("mock_jwt_token");
        when(jwtService.getExpiration()).thenReturn(3600L);
        when(userRoleRepository.findByUserId(any())).thenReturn(Collections.emptyList());

        VerifyDeviceChallengeCommandDTO command = VerifyDeviceChallengeCommandDTO.builder()
                .challengeSessionId(challengeSessionId)
                .code("123456")
                .tenantId(tenantId)
                .trustDevice(true)
                .build();

        AuthLoginView result = deviceSessionService.verifyChallenge(command);

        assertNotNull(result);
        assertEquals("AUTHENTICATED", result.status());
        verify(userDeviceRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve notificar senha alterada com quick-revoke quando deviceId presente")
    void shouldNotifyPasswordChangedWithRevokeWhenDevicePresent() {
        PasswordChangedNotifyCommand command = PasswordChangedNotifyCommand.builder()
                .codeUser(codeUser)
                .tenantId(tenantId)
                .email("test@example.com")
                .username("testuser")
                .deviceId("device_abc")
                .deviceName("Chrome Web")
                .deviceType("DESKTOP")
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla")
                .build();

        deviceSessionService.notifyPasswordChanged(command);

        verify(sessionCachePort, times(1)).saveQuickRevokeToken(any(), eq(172800L));

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(communicationClient, times(1)).sendMessage(payloadCaptor.capture(), eq(tenantId));

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("SENHA_ALTERADA_SUCESSO", payload.get("templateType"));
        assertEquals("test@example.com", payload.get("recipient"));

        @SuppressWarnings("unchecked")
        Map<String, Object> vars = (Map<String, Object>) payload.get("variables");
        assertNotNull(vars.get("revokeUrl"));
        assertFalse(((String) vars.get("revokeUrl")).isBlank());
        assertTrue(((String) vars.get("revokeUrl")).contains("quick-revoke"));
        assertEquals("Chrome Web", vars.get("deviceName"));
    }

    @Test
    @DisplayName("Deve notificar senha alterada sem revoke quando deviceId ausente")
    void shouldNotifyPasswordChangedWithoutRevokeWhenDeviceMissing() {
        PasswordChangedNotifyCommand command = PasswordChangedNotifyCommand.builder()
                .codeUser(codeUser)
                .tenantId(tenantId)
                .email("test@example.com")
                .username("testuser")
                .build();

        deviceSessionService.notifyPasswordChanged(command);

        verify(sessionCachePort, never()).saveQuickRevokeToken(any(), anyLong());

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(communicationClient, times(1)).sendMessage(payloadCaptor.capture(), eq(tenantId));

        @SuppressWarnings("unchecked")
        Map<String, Object> vars = (Map<String, Object>) payloadCaptor.getValue().get("variables");
        assertEquals("", vars.get("revokeUrl"));
    }
}

