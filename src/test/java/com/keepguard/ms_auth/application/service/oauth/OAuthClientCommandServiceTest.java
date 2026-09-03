package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenView;
import com.keepguard.ms_auth.application.mapper.OAuthClientApplicationMapper;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.port.out.persistence.OAuthClientRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientCreateCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientIdCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientUpdateCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthTokenCommandDTO;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemServiceRoleNames;
import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import com.keepguard.ms_auth.infrastructure.config.security.OAuthClientSecretCrypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private UUID serviceRoleId;

    @BeforeEach
    void setUp() {
        repository = mock(OAuthClientRepositoryPort.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        metricsPort = mock(MetricsPort.class);
        RoleRepositoryPort roleRepository = mock(RoleRepositoryPort.class);
        OAuthClientRoleResolver roleResolver = new OAuthClientRoleResolver(roleRepository);
        commandService = new OAuthClientCommandService(
                repository, passwordEncoder, jwtService, new OAuthClientApplicationMapper(), roleResolver, metricsPort,
                new OAuthClientSecretCrypto("test-base"));
        UUID serviceRoleId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Role serviceRole = Role.builder()
                .id(serviceRoleId)
                .name(SystemServiceRoleNames.ROLE_SERVICE_COLLECTOR)
                .companyId(null)
                .isSystem(true)
                .authorities(Set.of(
                        Authority.builder().id(UUID.randomUUID()).name("knowledge:read").build(),
                        Authority.builder().id(UUID.randomUUID()).name("knowledge:write").build()
                ))
                .build();
        when(roleRepository.findById(serviceRoleId)).thenReturn(Optional.of(serviceRole));
        this.serviceRoleId = serviceRoleId;
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
                .roleId(serviceRoleId)
                .build());

        assertNotNull(view.clientSecret());
        assertTrue(view.clientSecret().length() > 20);
        assertEquals(28800, view.tokenTtlSeconds());
        assertEquals("investbot-collector", view.clientId());
        verify(passwordEncoder).encode(view.clientSecret() + "test-base");
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
                eq(List.of("knowledge:read", "knowledge:write")), eq(28800_000L), eq(null), eq(null),
                eq(List.of(SystemServiceRoleNames.ROLE_SERVICE_COLLECTOR)))).thenReturn("jwt-token");

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
    @DisplayName("issueToken aceita hash composto com AUTH_CLIENT_SECRET_BASE")
    void issueToken_shouldAcceptComposedHash() {
        UUID clientUuid = UUID.randomUUID();
        OAuthClient client = activeClient(clientUuid);
        when(repository.findByCompanyIdAndClientId(companyId, "investbot-collector")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("plain-secret", "hashed-secret")).thenReturn(false);
        when(passwordEncoder.matches("plain-secrettest-base", "hashed-secret")).thenReturn(true);
        when(jwtService.generateServiceToken(eq(clientUuid), eq("investbot-collector"), eq(companyId),
                eq(List.of("knowledge:read", "knowledge:write")), eq(28800_000L), eq(null), eq(null),
                eq(List.of(SystemServiceRoleNames.ROLE_SERVICE_COLLECTOR)))).thenReturn("jwt-composed");

        OAuthTokenView view = commandService.issueToken(OAuthTokenCommandDTO.builder()
                .companyId(companyId)
                .grantType("client_credentials")
                .clientId("investbot-collector")
                .clientSecret("plain-secret")
                .build());

        assertEquals("jwt-composed", view.accessToken());
    }

    @Test
    @DisplayName("issueToken propaga agentId e agentCode para o JWT")
    void issueToken_shouldPropagateAgentClaims() {
        UUID clientUuid = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        UUID agentCode = UUID.randomUUID();
        OAuthClient client = activeClient(clientUuid);
        when(repository.findByCompanyIdAndClientId(companyId, "investbot-collector")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("plain-secret", "hashed-secret")).thenReturn(true);
        when(jwtService.generateServiceToken(eq(clientUuid), eq("investbot-collector"), eq(companyId),
                eq(List.of("knowledge:read", "knowledge:write")), eq(28800_000L), eq(agentId.toString()), eq(agentCode.toString()),
                eq(List.of(SystemServiceRoleNames.ROLE_SERVICE_COLLECTOR))))
                .thenReturn("jwt-with-agent");

        OAuthTokenView view = commandService.issueToken(OAuthTokenCommandDTO.builder()
                .companyId(companyId)
                .grantType("client_credentials")
                .clientId("investbot-collector")
                .clientSecret("plain-secret")
                .agentId(agentId.toString())
                .agentCode(agentCode.toString())
                .build());

        assertEquals("jwt-with-agent", view.accessToken());
    }

    @Test
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
        verify(jwtService, never()).generateServiceToken(any(), any(), any(), any(), anyLong(), any(), any());
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

    @Test
    @DisplayName("create rejeita role de empresa")
    void create_shouldRejectCompanyRole() {
        UUID companyRoleId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Role companyRole = Role.builder()
                .id(companyRoleId)
                .name("ROLE_ADMIN")
                .companyId(companyId)
                .isSystem(true)
                .build();
        RoleRepositoryPort roleRepository = mock(RoleRepositoryPort.class);
        when(roleRepository.findById(companyRoleId)).thenReturn(Optional.of(companyRole));
        OAuthClientCommandService service = new OAuthClientCommandService(
                repository, passwordEncoder, jwtService, new OAuthClientApplicationMapper(),
                new OAuthClientRoleResolver(roleRepository), metricsPort,
                new OAuthClientSecretCrypto("test-base"));
        ReflectionTestUtils.setField(service, "minTtlSeconds", 900);
        ReflectionTestUtils.setField(service, "defaultTtlSeconds", 28800);
        ReflectionTestUtils.setField(service, "maxTtlSeconds", 86400);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(
                OAuthClientCreateCommandDTO.builder()
                        .companyId(companyId)
                        .clientId("bad-role")
                        .roleId(companyRoleId)
                        .build()));
        assertTrue(ex.getMessage().contains("service role"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update altera descrição, TTL e service role sem regenerar secret")
    void update_shouldChangeMetadataWithoutRotatingSecret() {
        UUID id = UUID.randomUUID();
        OAuthClient client = activeClient(id);
        client.setDescription("antes");
        when(repository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(client));
        when(repository.save(any(OAuthClient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuthClientView view = commandService.update(OAuthClientUpdateCommandDTO.builder()
                .companyId(companyId)
                .id(id)
                .description("collector atualizado")
                .roleId(serviceRoleId)
                .tokenTtlSeconds(3600)
                .build());

        assertEquals("collector atualizado", view.description());
        assertEquals(3600, view.tokenTtlSeconds());
        assertEquals(serviceRoleId, view.serviceRoleId());
        assertEquals(SystemServiceRoleNames.ROLE_SERVICE_COLLECTOR, view.serviceRoleName());
        verify(passwordEncoder, never()).encode(anyString());
        verify(metricsPort).incrementCounter(eq("oauth_client_updated_total"), anyMap());
    }

    @Test
    @DisplayName("update rejeita role de empresa")
    void update_shouldRejectCompanyRole() {
        UUID id = UUID.randomUUID();
        UUID companyRoleId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Role companyRole = Role.builder()
                .id(companyRoleId)
                .name("ROLE_ADMIN")
                .companyId(companyId)
                .isSystem(true)
                .build();
        RoleRepositoryPort roleRepository = mock(RoleRepositoryPort.class);
        when(roleRepository.findById(companyRoleId)).thenReturn(Optional.of(companyRole));
        OAuthClientRepositoryPort repo = mock(OAuthClientRepositoryPort.class);
        when(repo.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(activeClient(id)));
        OAuthClientCommandService service = new OAuthClientCommandService(
                repo, passwordEncoder, jwtService, new OAuthClientApplicationMapper(),
                new OAuthClientRoleResolver(roleRepository), metricsPort,
                new OAuthClientSecretCrypto("test-base"));
        ReflectionTestUtils.setField(service, "minTtlSeconds", 900);
        ReflectionTestUtils.setField(service, "defaultTtlSeconds", 28800);
        ReflectionTestUtils.setField(service, "maxTtlSeconds", 86400);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(
                OAuthClientUpdateCommandDTO.builder()
                        .companyId(companyId)
                        .id(id)
                        .roleId(companyRoleId)
                        .tokenTtlSeconds(28800)
                        .build()));
        assertTrue(ex.getMessage().contains("service role"));
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("update rejeita TTL inválido")
    void update_shouldRejectInvalidTtl() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(activeClient(id)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> commandService.update(
                OAuthClientUpdateCommandDTO.builder()
                        .companyId(companyId)
                        .id(id)
                        .roleId(serviceRoleId)
                        .tokenTtlSeconds(899)
                        .build()));
        assertTrue(ex.getMessage().contains("tokenTtlSeconds"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lança NotFound quando client não pertence à empresa")
    void update_shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commandService.update(OAuthClientUpdateCommandDTO.builder()
                .companyId(companyId)
                .id(id)
                .roleId(serviceRoleId)
                .tokenTtlSeconds(28800)
                .build()));
        verify(repository, never()).save(any());
    }

    private OAuthClient activeClient(UUID id) {
        return OAuthClient.builder()
                .id(id)
                .companyId(companyId)
                .clientId("investbot-collector")
                .secretHash("hashed-secret")
                .serviceRoleId(serviceRoleId)
                .authorities(List.of())
                .status(OAuthClientStatus.ACTIVE)
                .tokenTtlSeconds(28800)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
