package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.user.UserRole;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserRoleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserRoleJpaMapper {

    public UserRoleJpaEntity toJpaEntity(UserRole domain) {
        if (domain == null) {
            return null;
        }

        return UserRoleJpaEntity.builder()
                .userId(domain.getUserId())
                .roleId(domain.getRoleId())
                .assignedAt(domain.getAssignedAt())
                .build();
    }

    public UserRole toDomain(UserRoleJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return UserRole.builder()
                .userId(jpaEntity.getUserId())
                .roleId(jpaEntity.getRoleId())
                .assignedAt(jpaEntity.getAssignedAt())
                .build();
    }
}
