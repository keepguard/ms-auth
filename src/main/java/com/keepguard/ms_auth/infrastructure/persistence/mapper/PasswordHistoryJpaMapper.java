package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.user.PasswordHistory;
import com.keepguard.ms_auth.infrastructure.persistence.entity.PasswordHistoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordHistoryJpaMapper {

    public PasswordHistoryJpaEntity toJpaEntity(PasswordHistory domain) {
        if (domain == null) {
            return null;
        }

        return PasswordHistoryJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .passwordHash(domain.getPasswordHash())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public PasswordHistory toDomain(PasswordHistoryJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return PasswordHistory.builder()
                .id(jpaEntity.getId())
                .userId(jpaEntity.getUserId())
                .passwordHash(jpaEntity.getPasswordHash())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }
}
