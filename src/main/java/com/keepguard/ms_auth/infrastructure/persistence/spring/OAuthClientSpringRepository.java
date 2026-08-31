package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import com.keepguard.ms_auth.infrastructure.persistence.entity.OAuthClientJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthClientSpringRepository extends JpaRepository<OAuthClientJpaEntity, UUID> {

    Optional<OAuthClientJpaEntity> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<OAuthClientJpaEntity> findByCompanyIdAndClientId(UUID companyId, String clientId);

    List<OAuthClientJpaEntity> findAllByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    @Query("""
            SELECT c FROM OAuthClientJpaEntity c
            WHERE c.companyId = :companyId
              AND (:clientId = '' OR LOWER(c.clientId) LIKE LOWER(CONCAT('%', :clientId, '%')))
              AND (:statusEnabled = false OR c.status = :status)
            """)
    Page<OAuthClientJpaEntity> search(
            @Param("companyId") UUID companyId,
            @Param("clientId") String clientId,
            @Param("statusEnabled") boolean statusEnabled,
            @Param("status") OAuthClientStatus status,
            Pageable pageable);
}
