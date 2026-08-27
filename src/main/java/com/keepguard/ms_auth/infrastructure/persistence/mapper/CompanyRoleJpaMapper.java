package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.role.CompanyRole;
import com.keepguard.ms_auth.infrastructure.persistence.entity.CompanyRoleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CompanyRoleJpaMapper {

    public CompanyRoleJpaEntity toJpaEntity(CompanyRole domain) {
        if (domain == null) {
            return null;
        }
        return CompanyRoleJpaEntity.builder()
                .companyId(domain.getCompanyId())
                .roleId(domain.getRoleId())
                .enabled(domain.isEnabled())
                .defaultRole(domain.isDefaultRole())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public CompanyRole toDomain(CompanyRoleJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return CompanyRole.builder()
                .companyId(jpaEntity.getCompanyId())
                .roleId(jpaEntity.getRoleId())
                .enabled(jpaEntity.isEnabled())
                .defaultRole(jpaEntity.isDefaultRole())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }
}
