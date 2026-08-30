package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthClientCommandService {

    private static final String GRANT_TYPE = "client_credentials";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OAuthClientRepositoryPort oauthClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OAuthClientApplicationMapper mapper;
    private final MetricsPort metricsPort;

    @Value("${security.jwt.service-token.min-ttl-seconds:900}")
    private int minTtlSeconds;

    @Value("${security.jwt.service-token.default-ttl-seconds:28800}")
    private int defaultTtlSeconds;

    @Value("${security.jwt.service-token.max-ttl-seconds:86400}")
    private int maxTtlSeconds;

    @LogOperation(
            operation = "CREATE_OAUTH_CLIENT",
            description = "Criando oauth client: {command.clientId}",
            audit = true,
            auditAction = "CREATE",
            auditEntityType = "OAUTH_CLIENT"
    )
    @Transactional
    public OAuthClientCreateView create(OAuthClientCreateCommandDTO command) {
        String clientId = requireClientId(command.getClientId());
        UUID companyId = requireCompanyId(command.getCompanyId());
        int ttl = resolveTtl(command.getTokenTtlSeconds());

        if (oauthClientRepository.findByCompanyIdAndClientId(companyId, clientId).isPresent()) {
            metricsPort.incrementCounter("oauth_client_business_errors_total",
                    Map.of("error_type", "client_already_exists", "operation", "create"));
            throw new AlreadyExistsException("OAuth client já existe: " + clientId);
        }

        String plainSecret = generateSecret();
        LocalDateTime now = LocalDateTime.now();
        OAuthClient client = OAuthClient.builder()
                .companyId(companyId)
                .clientId(clientId)
                .secretHash(passwordEncoder.encode(plainSecret))
                .authorities(normalizeAuthorities(command.getAuthorities()))
                .status(OAuthClientStatus.ACTIVE)
                .tokenTtlSeconds(ttl)
                .description(trimToNull(command.getDescription()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OAuthClient saved = oauthClientRepository.save(client);
        metricsPort.incrementCounter("oauth_client_created_total",
                Map.of("client_id", clientId));
        return mapper.toCreateView(saved, plainSecret);
    }

    @LogOperation(
            operation = "BLOCK_OAUTH_CLIENT",
            description = "Bloqueando oauth client: {command.id}",
            audit = true,
            auditAction = "UPDATE",
            auditEntityType = "OAUTH_CLIENT"
    )
    @Transactional
    public OAuthClientView block(OAuthClientIdCommandDTO command) {
        OAuthClient client = requireClient(command.getCompanyId(), command.getId(), "block");
        client.block();
        OAuthClient saved = oauthClientRepository.save(client);
        metricsPort.incrementCounter("oauth_client_blocked_total",
                Map.of("client_id", saved.getClientId()));
        return mapper.toView(saved);
    }

    @LogOperation(
            operation = "UNBLOCK_OAUTH_CLIENT",
            description = "Desbloqueando oauth client: {command.id}",
            audit = true,
            auditAction = "UPDATE",
            auditEntityType = "OAUTH_CLIENT"
    )
    @Transactional
    public OAuthClientView unblock(OAuthClientIdCommandDTO command) {
        OAuthClient client = requireClient(command.getCompanyId(), command.getId(), "unblock");
        client.unblock();
        OAuthClient saved = oauthClientRepository.save(client);
        metricsPort.incrementCounter("oauth_client_unblocked_total",
                Map.of("client_id", saved.getClientId()));
        return mapper.toView(saved);
    }

    @LogOperation(
            operation = "DELETE_OAUTH_CLIENT",
            description = "Removendo oauth client: {command.id}",
            audit = true,
            auditAction = "DELETE",
            auditEntityType = "OAUTH_CLIENT"
    )
    @Transactional
    public void delete(OAuthClientIdCommandDTO command) {
        OAuthClient client = requireClient(command.getCompanyId(), command.getId(), "delete");
        oauthClientRepository.delete(client);
        metricsPort.incrementCounter("oauth_client_deleted_total",
                Map.of("client_id", client.getClientId()));
    }

    @Transactional(readOnly = true)
    public OAuthTokenView issueToken(OAuthTokenCommandDTO command) {
        UUID companyId = requireCompanyId(command.getCompanyId());
        if (command.getGrantType() == null || !GRANT_TYPE.equalsIgnoreCase(command.getGrantType().trim())) {
            throw new IllegalArgumentException("grantType deve ser client_credentials.");
        }
        String clientId = requireClientId(command.getClientId());
        if (command.getClientSecret() == null || command.getClientSecret().isBlank()) {
            throw new InvalidCredentialsException();
        }

        OAuthClient client = oauthClientRepository.findByCompanyIdAndClientId(companyId, clientId)
                .orElseThrow(InvalidCredentialsException::new);

        if (!client.isActive() || !passwordEncoder.matches(command.getClientSecret(), client.getSecretHash())) {
            metricsPort.incrementCounter("oauth_token_business_errors_total",
                    Map.of("error_type", "invalid_client", "operation", "issue_token"));
            throw new InvalidCredentialsException();
        }

        long ttlMillis = client.getTokenTtlSeconds() * 1000L;
        String token = jwtService.generateServiceToken(
                client.getId(),
                client.getClientId(),
                client.getCompanyId(),
                client.getAuthorities(),
                ttlMillis
        );
        metricsPort.incrementCounter("oauth_token_issued_total",
                Map.of("client_id", client.getClientId()));
        return new OAuthTokenView(token, "Bearer", client.getTokenTtlSeconds());
    }

    int resolveTtl(Integer requested) {
        if (requested == null) {
            return defaultTtlSeconds;
        }
        if (requested < minTtlSeconds || requested > maxTtlSeconds) {
            throw new IllegalArgumentException(
                    "tokenTtlSeconds deve estar entre " + minTtlSeconds + " e " + maxTtlSeconds + " segundos."
            );
        }
        return requested;
    }

    private OAuthClient requireClient(UUID companyId, UUID id, String operation) {
        if (companyId == null || id == null) {
            throw new IllegalArgumentException("companyId e id são obrigatórios.");
        }
        return oauthClientRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("oauth_client_business_errors_total",
                            Map.of("error_type", "client_not_found", "operation", operation));
                    return new NotFoundException("OAuth client não encontrado.");
                });
    }

    private UUID requireCompanyId(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("X-Company-Id é obrigatório.");
        }
        return companyId;
    }

    private String requireClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId é obrigatório.");
        }
        String trimmed = clientId.trim();
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("clientId deve ter no máximo 100 caracteres.");
        }
        return trimmed;
    }

    private List<String> normalizeAuthorities(List<String> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String authority : authorities) {
            if (authority != null && !authority.isBlank()) {
                normalized.add(authority.trim());
            }
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
