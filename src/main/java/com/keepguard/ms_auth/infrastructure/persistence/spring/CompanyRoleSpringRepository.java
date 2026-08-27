package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.CompanyRoleIdJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.entity.CompanyRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRoleSpringRepository extends JpaRepository<CompanyRoleJpaEntity, CompanyRoleIdJpaEntity> {

    boolean existsByCompanyId(UUID companyId);

    Optional<CompanyRoleJpaEntity> findByCompanyIdAndRoleId(UUID companyId, UUID roleId);

    List<CompanyRoleJpaEntity> findByCompanyIdAndEnabledTrueAndDefaultRoleTrue(UUID companyId);

    List<CompanyRoleJpaEntity> findByCompanyId(UUID companyId);

    void deleteByCompanyIdAndRoleId(UUID companyId, UUID roleId);
}
