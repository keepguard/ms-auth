package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.application.mapper.OAuthClientApplicationMapper;
import com.keepguard.ms_auth.application.port.out.persistence.OAuthClientRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthClientQueryService {

    private final OAuthClientRepositoryPort oauthClientRepository;
    private final OAuthClientApplicationMapper mapper;

    @Transactional(readOnly = true)
    public OAuthClientView findById(UUID companyId, UUID id) {
        if (companyId == null || id == null) {
            throw new IllegalArgumentException("companyId e id são obrigatórios.");
        }
        return oauthClientRepository.findByIdAndCompanyId(id, companyId)
                .map(mapper::toView)
                .orElseThrow(() -> new NotFoundException("OAuth client não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<OAuthClientView> listByCompany(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("X-Company-Id é obrigatório.");
        }
        return oauthClientRepository.findAllByCompanyId(companyId).stream()
                .map(mapper::toView)
                .toList();
    }
}
