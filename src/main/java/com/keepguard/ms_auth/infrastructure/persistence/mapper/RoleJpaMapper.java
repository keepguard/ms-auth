package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.infrastructure.persistence.entity.AuthorityJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleJpaMapper {

    public RoleJpaEntity toJpaEntity(Role domain) {
        if (domain == null) {
            return null;
        }

        return RoleJpaEntity.builder()
                .id(domain.getId())
                .companyId(domain.getCompanyId())
                .name(domain.getName())
                .description(domain.getDescription())
                .isSystem(domain.isSystem())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Role toDomain(RoleJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        var role = Role.builder()
                .id(jpaEntity.getId())
                .companyId(jpaEntity.getCompanyId())
                .name(jpaEntity.getName())
                .description(jpaEntity.getDescription())
                .isSystem(jpaEntity.isSystem())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();

        if (jpaEntity.getAuthorities() != null && !jpaEntity.getAuthorities().isEmpty()) {
            var authoritiesDomain = jpaEntity.getAuthorities().stream()
                    .map(this::authorityToDomain)
                    .collect(Collectors.toSet());
            role.setAuthorities(authoritiesDomain);
        }

        return role;
    }

    // Helper method para converter Authority (usado no toDomain)
    private Authority authorityToDomain(AuthorityJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Authority.builder()
                .id(jpaEntity.getId())
                .companyId(jpaEntity.getCompanyId())
                .name(jpaEntity.getName())
                .description(jpaEntity.getDescription())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }
}
