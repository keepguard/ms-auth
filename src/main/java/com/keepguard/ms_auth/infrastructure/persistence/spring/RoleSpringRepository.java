package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleSpringRepository extends JpaRepository<RoleJpaEntity, UUID> {
    Optional<RoleJpaEntity> findByName(String name);

    Optional<RoleJpaEntity> findByCompanyIdAndName(UUID companyId, String name);

    Optional<RoleJpaEntity> findByCompanyIdIsNullAndName(String name);

    List<RoleJpaEntity> findByCompanyId(UUID companyId);

    Page<RoleJpaEntity> findByCompanyId(UUID companyId, Pageable pageable);
}
