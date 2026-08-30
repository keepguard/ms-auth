package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.OAuthClientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthClientSpringRepository extends JpaRepository<OAuthClientJpaEntity, UUID> {

    Optional<OAuthClientJpaEntity> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<OAuthClientJpaEntity> findByCompanyIdAndClientId(UUID companyId, String clientId);

    List<OAuthClientJpaEntity> findAllByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
