package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.role.CompanyRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRoleRepositoryPort {

    CompanyRole save(CompanyRole companyRole);

    boolean existsByCompanyId(UUID companyId);

    Optional<CompanyRole> findByCompanyIdAndRoleId(UUID companyId, UUID roleId);

    List<CompanyRole> findEnabledDefaultsByCompanyId(UUID companyId);

    List<CompanyRole> findByCompanyId(UUID companyId);

    void deleteByCompanyIdAndRoleId(UUID companyId, UUID roleId);
}
