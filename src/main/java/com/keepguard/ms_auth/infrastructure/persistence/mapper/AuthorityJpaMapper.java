package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.infrastructure.persistence.entity.AuthorityJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthorityJpaMapper {

    public AuthorityJpaEntity toJpaEntity(Authority domain) {
        if (domain == null) {
            return null;
        }

        return AuthorityJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Authority toDomain(AuthorityJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return Authority.builder()
                .id(jpaEntity.getId())
                .name(jpaEntity.getName())
                .description(jpaEntity.getDescription())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }
}

