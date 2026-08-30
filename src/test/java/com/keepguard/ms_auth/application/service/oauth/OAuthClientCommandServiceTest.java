package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenView;
import com.keepguard.ms_auth.application.mapper.OAuthClientApplicationMapper;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.port.out.persistence.OAuthClientRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientCreateCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientIdCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthTokenCommandDTO;
import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OAuthClientCommandService Tests")
class OAuthClientCommandServiceTest {

    private OAuthClientCommandService commandService;
    private OAuthClientRepositoryPort repository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private MetricsPort metricsPort;

    private final UUID companyId = UUID.fromString("f7fc7350-b9fc-4e54-9c58-ac9385b23ae4");

    @BeforeEach
    void setUp() {
        repository = mock(OAuthClientRepositoryPort.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        metricsPort = mock(MetricsPort.class);
        commandService = new OAuthClientCommandService(
                repository, passwordEncoder, jwtService, new OAuthClientApplicationMapper(), metricsPort);
        ReflectionTestUtils.setField(commandService, "minTtlSeconds", 900);
        ReflectionTestUtils.setField(commandService, "defaultTtlSeconds", 28800);
        ReflectionTestUtils.setField(commandService, "maxTtlSeconds", 86400);
    }

    @Test
    @DisplayName("create usa TTL padrão de 8h e devolve secret uma vez")
    void create_shouldUseDefaultTtlAndReturnSecretOnce() {
        when(repository.findByCompanyIdAndClientId(companyId, "investbot-collector")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-secret");
        when(repository.save(any(OAuthClient.class))).thenAnswer(invocation -> {
            OAuthClient client = invocation.getArgument(0);
            client.setId(UUID.randomUUID());
            return client;
        });

        OAuthClientCreateView view = commandService.create(OAuthClientCreateCommandDTO.builder()
                .companyId(companyId)
                .clientId("investbot-collector")
                .authorities(List.of("knowledge:write"))
                .build());

        assertNotNull(view.clientSecret());
        assertTrue(view.clientSecret().length() > 20);
        assertEquals(28800, view.tokenTtlSeconds());
        assertEquals("investbot-collector", view.clientId());
        verify(passwordEncoder).encode(view.clientSecret());
        verify(metricsPort).incrementCounter(eq("oauth_client_created_total"), anyMap());
    }

    @Test
    @DisplayName("create rejeita TTL abaixo do mínimo")
    void create_shouldRejectTtlBelowMin() {
        var cmd = OAuthClientCreateCommandDTO.builder()
                .companyId(companyId)
                .clientId("too-short")
                .tokenTtlSeconds(899)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> commandService.create(cmd));
        assertTrue(ex.getMessage().contains("tokenTtlSeconds"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create rejeita TTL acima de 24h")
    void create_shouldRejectTtlAboveMax() {
        var cmd = OAuthClientCreateCommandDTO.builder()
                .companyId(companyId)
                .clientId("too-long")
                .tokenTtlSeconds(86401)
                .build();

        assertThrows(IllegalArgumentException.class, () -> commandService.create(cmd));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lança AlreadyExists quando clientId já existe na empresa")
    void create_shouldThrowWhenClientExists() {
        when(repository.findByCompanyIdAndClientId(companyId, "investbot-collector"))
                .thenReturn(Optional.of(OAuthClient.builder().id(UUID.randomUUID()).build()));

        assertThrows(AlreadyExistsException.class, () -> commandService.create(OAuthClientCreateCommandDTO.builder()
                .companyId(companyId)
                .clientId("investbot-collector")
                .build()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("issueToken emite JWT com TTL do client")
    void issueToken_shouldIssueJwt() {
        UUID clientUuid = UUID.randomUUID();
        OAuthClient client = activeClient(clientUuid);
        when(repository.findByCompanyIdAndClientId(companyId, "investbot-collector")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("plain-secret", "hashed-secret")).thenReturn(true);
        when(jwtService.generateServiceToken(eq(clientUuid), eq("investbot-collector"), eq(companyId),
                eq(List.of("knowledge:write")), eq(28800_000L))).thenReturn("jwt-token");

        OAuthTokenView view = commandService.issueToken(OAuthTokenCommandDTO.builder()
                .companyId(companyId)
                .grantType("client_credentials")
                .clientId("investbot-collector")
                .clientSecret("plain-secret")
                .build());

        assertEquals("jwt-token", view.accessToken());
        assertEquals("Bearer", view.tokenType());
        assertEquals(28800, view.expiresIn());
    }

    @Test
    @DisplayName("issueToken recusa client bloqueado")
    void issueToken_shouldRejectBlockedClient() {
        OAuthClient client = activeClient(UUID.randomUUID());
        client.setStatus(OAuthClientStatus.BLOCKED);
        when(repository.findByCompanyIdAndClientId(companyId, "investbot-collector")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("plain-secret", "hashed-secret")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () -> commandService.issueToken(OAuthTokenCommandDTO.builder()
                .companyId(companyId)
                .grantType("client_credentials")
                .clientId("investbot-collector")
                .clientSecret("plain-secret")
                .build()));
        verify(jwtService, never()).generateServiceToken(any(), any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("issueToken recusa companyId que não bate com o client")
    void issueToken_shouldRejectUnknownCompanyClientPair() {
        when(repository.findByCompanyIdAndClientId(companyId, "investbot-collector")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> commandService.issueToken(OAuthTokenCommandDTO.builder()
                .companyId(companyId)
                .grantType("client_credentials")
                .clientId("investbot-collector")
                .clientSecret("plain-secret")
                .build()));
    }

    @Test
    @DisplayName("block lança NotFound quando client não pertence à empresa")
    void block_shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commandService.block(OAuthClientIdCommandDTO.builder()
                .companyId(companyId)
                .id(id)
                .build()));
    }

    private OAuthClient activeClient(UUID id) {
        return OAuthClient.builder()
                .id(id)
                .companyId(companyId)
                .clientId("investbot-collector")
                .secretHash("hashed-secret")
                .authorities(List.of("knowledge:write"))
                .status(OAuthClientStatus.ACTIVE)
                .tokenTtlSeconds(28800)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
