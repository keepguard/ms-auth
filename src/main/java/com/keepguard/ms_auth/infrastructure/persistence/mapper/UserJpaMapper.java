package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserJpaMapper {

    public UserJpaEntity toJpaEntity(User domain) {
        if (domain == null) {
            return null;
        }

        return UserJpaEntity.builder()
                .id(domain.getId())
                .idUserExternal(domain.getIdUserExternal())
                .codeUser(domain.getCodeUser())
                .username(domain.getUsername())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .status(domain.getStatus())
                .emailVerified(domain.getEmailVerified())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .lastLogin(domain.getLastLogin())
                .companyId(domain.getCompanyId())
                .companyCode(domain.getCompanyCode())
                .xApplication(domain.getXApplication())
                .build();
    }

    public User toDomain(UserJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return User.builder()
                .id(jpaEntity.getId())
                .idUserExternal(jpaEntity.getIdUserExternal())
                .codeUser(jpaEntity.getCodeUser())
                .username(jpaEntity.getUsername())
                .email(jpaEntity.getEmail())
                .passwordHash(jpaEntity.getPasswordHash())
                .status(jpaEntity.getStatus())
                .emailVerified(jpaEntity.getEmailVerified())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .lastLogin(jpaEntity.getLastLogin())
                .companyId(jpaEntity.getCompanyId())
                .companyCode(jpaEntity.getCompanyCode())
                .xApplication(jpaEntity.getXApplication())
                .build();
    }
}
