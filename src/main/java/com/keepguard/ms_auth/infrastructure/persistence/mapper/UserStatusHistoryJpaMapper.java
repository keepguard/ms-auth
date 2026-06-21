package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserStatusHistoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserStatusHistoryJpaMapper {

    public UserStatusHistoryJpaEntity toJpaEntity(UserStatusHistory domain) {
        if (domain == null) {
            return null;
        }

        return UserStatusHistoryJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .eventType(domain.getEventType())
                .reason(domain.getReason())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public UserStatusHistory toDomain(UserStatusHistoryJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return UserStatusHistory.builder()
                .id(jpaEntity.getId())
                .userId(jpaEntity.getUserId())
                .eventType(jpaEntity.getEventType())
                .reason(jpaEntity.getReason())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }
}
