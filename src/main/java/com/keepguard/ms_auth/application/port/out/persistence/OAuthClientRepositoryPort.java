package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OAuthClientRepositoryPort {

    OAuthClient save(OAuthClient client);

    Optional<OAuthClient> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<OAuthClient> findByCompanyIdAndClientId(UUID companyId, String clientId);

    List<OAuthClient> findAllByCompanyId(UUID companyId);

    Page<OAuthClient> search(UUID companyId, String clientId, OAuthClientStatus status, Pageable pageable);

    void delete(OAuthClient client);
}
