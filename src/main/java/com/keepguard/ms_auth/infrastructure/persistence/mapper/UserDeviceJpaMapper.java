package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.session.UserDevice;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserDeviceJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserDeviceJpaMapper {

    public UserDeviceJpaEntity toJpaEntity(UserDevice domain) {
        if (domain == null) {
            return null;
        }
        return UserDeviceJpaEntity.builder()
                .id(domain.getId())
                .codeUser(domain.getCodeUser())
                .tenantId(domain.getTenantId())
                .deviceId(domain.getDeviceId())
                .deviceName(domain.getDeviceName())
                .deviceType(domain.getDeviceType())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .isTrusted(Boolean.TRUE.equals(domain.getIsTrusted()))
                .firstSeenAt(domain.getFirstSeenAt())
                .lastActiveAt(domain.getLastActiveAt())
                .revokedAt(domain.getRevokedAt())
                .build();
    }

    public UserDevice toDomain(UserDeviceJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return UserDevice.builder()
                .id(jpaEntity.getId())
                .codeUser(jpaEntity.getCodeUser())
                .tenantId(jpaEntity.getTenantId())
                .deviceId(jpaEntity.getDeviceId())
                .deviceName(jpaEntity.getDeviceName())
                .deviceType(jpaEntity.getDeviceType())
                .ipAddress(jpaEntity.getIpAddress())
                .userAgent(jpaEntity.getUserAgent())
                .isTrusted(jpaEntity.getIsTrusted())
                .firstSeenAt(jpaEntity.getFirstSeenAt())
                .lastActiveAt(jpaEntity.getLastActiveAt())
                .revokedAt(jpaEntity.getRevokedAt())
                .build();
    }
}
