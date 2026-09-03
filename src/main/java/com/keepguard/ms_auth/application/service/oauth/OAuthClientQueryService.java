package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthServiceRoleAuthorityView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthServiceRoleView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientRuntimeSecretView;
import com.keepguard.ms_auth.application.mapper.OAuthClientApplicationMapper;
import com.keepguard.ms_auth.application.port.out.persistence.OAuthClientRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientSearchQueryDTO;
import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemServiceRoleNames;
import com.keepguard.ms_auth.infrastructure.config.security.OAuthClientSecretCrypto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthClientQueryService {

    private static final Set<String> ALLOWED_SORT = Set.of("createdAt", "clientId", "status");

    private final OAuthClientRepositoryPort oauthClientRepository;
    private final RoleRepositoryPort roleRepository;
    private final OAuthClientApplicationMapper mapper;
    private final OAuthClientRoleResolver roleResolver;
    private final OAuthClientSecretCrypto secretCrypto;

    @Transactional(readOnly = true)
    public OAuthClientView findById(UUID companyId, UUID id) {
        if (companyId == null || id == null) {
            throw new IllegalArgumentException("companyId e id são obrigatórios.");
        }
        return oauthClientRepository.findByIdAndCompanyId(id, companyId)
                .map(roleResolver::enrich)
                .map(this::toSecretView)
                .orElseThrow(() -> new NotFoundException("OAuth client não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<OAuthClientView> listByCompany(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("X-Company-Id é obrigatório.");
        }
        return oauthClientRepository.findAllByCompanyId(companyId).stream()
                .map(roleResolver::enrich)
                .map(this::toSecretView)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResultView<OAuthClientView> search(OAuthClientSearchQueryDTO query) {
        if (query == null || query.getCompanyId() == null) {
            throw new IllegalArgumentException("X-Company-Id é obrigatório.");
        }
        String clientId = StringUtils.hasText(query.getClientId()) ? query.getClientId().trim() : null;
        Pageable pageable = sanitizePageable(query.getPageable());
        Page<OAuthClientView> page = oauthClientRepository
                .search(query.getCompanyId(), clientId, query.getStatus(), pageable)
                .map(roleResolver::enrich)
                .map(this::toSecretView);
        return PageResultView.<OAuthClientView>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public List<OAuthServiceRoleView> listServiceRoles() {
        return SystemServiceRoleNames.SERVICE_TEMPLATES.stream()
                .map(roleRepository::findByCompanyIdIsNullAndName)
                .flatMap(java.util.Optional::stream)
                .map(this::toServiceRoleView)
                .toList();
    }

    @Transactional(readOnly = true)
    public OAuthClientRuntimeSecretView findRuntimeSecret(UUID companyId, String clientId, String presentedBase) {
        if (!secretCrypto.matchesBase(presentedBase)) {
            throw new InvalidCredentialsException();
        }
        if (companyId == null) {
            throw new IllegalArgumentException("X-Company-Id é obrigatório.");
        }
        String resolvedClientId = StringUtils.hasText(clientId) ? clientId.trim() : "srv-data-collector";
        OAuthClient client = oauthClientRepository.findByCompanyIdAndClientId(companyId, resolvedClientId)
                .orElseThrow(() -> new NotFoundException("OAuth client não encontrado."));
        if (!client.isActive()) {
            throw new NotFoundException("OAuth client não encontrado.");
        }
        if (!StringUtils.hasText(client.getSecretEncrypted())) {
            throw new NotFoundException("OAuth client sem secret cifrado. Recrie o client.");
        }
        return new OAuthClientRuntimeSecretView(client.getClientId(), client.getSecretEncrypted(), client.getStatus());
    }

    private OAuthClientView toSecretView(OAuthClient client) {
        return mapper.toView(client, secretCrypto.decryptOrNull(client.getSecretEncrypted()));
    }

    private OAuthServiceRoleView toServiceRoleView(Role role) {
        List<OAuthServiceRoleAuthorityView> authorities = role.getAuthorities() == null
                ? List.of()
                : role.getAuthorities().stream()
                .map(authority -> new OAuthServiceRoleAuthorityView(authority.getName(), authority.getDescription()))
                .toList();
        return new OAuthServiceRoleView(role.getId(), role.getName(), role.getDescription(), authorities);
    }

    private Pageable sanitizePageable(Pageable pageable) {
        int page = pageable != null ? Math.max(pageable.getPageNumber(), 0) : 0;
        int size = pageable != null ? pageable.getPageSize() : 20;
        if (size <= 0) {
            size = 20;
        }
        size = Math.min(size, 100);

        String property = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;
        if (pageable != null && pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            if (ALLOWED_SORT.contains(order.getProperty())) {
                property = order.getProperty();
            }
            direction = order.getDirection() == null ? Sort.Direction.DESC : order.getDirection();
        }
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
