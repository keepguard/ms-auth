package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;
import com.keepguard.ms_auth.infrastructure.persistence.entity.DeviceBlacklistJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DeviceBlacklistJpaMapper {

    public DeviceBlacklistJpaEntity toJpaEntity(DeviceBlacklistEntry domain) {
        if (domain == null) {
            return null;
        }

        UUID codeUserUuid = domain.getCodeUser() != null ? UUID.fromString(domain.getCodeUser()) : null;
        LocalDateTime blockedAtTime = LocalDateTime.now();
        if (domain.getBlockedAt() != null && !domain.getBlockedAt().isBlank()) {
            try {
                blockedAtTime = LocalDateTime.parse(domain.getBlockedAt());
            } catch (Exception ignored) {
                blockedAtTime = LocalDateTime.now();
            }
        }

        return DeviceBlacklistJpaEntity.builder()
                .id(domain.getId())
                .companyId(domain.getCompanyId())
                .codeUser(codeUserUuid)
                .deviceId(domain.getDeviceId())
                .deviceName(domain.getDeviceName())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .reason(domain.getReason())
                .blockedBy(domain.getBlockedBy())
                .blockedAt(blockedAtTime)
                .expiresAt(domain.getExpiresAt())
                .build();
    }

    public DeviceBlacklistEntry toDomain(DeviceBlacklistJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return DeviceBlacklistEntry.builder()
                .id(jpaEntity.getId())
                .companyId(jpaEntity.getCompanyId())
                .codeUser(jpaEntity.getCodeUser() != null ? jpaEntity.getCodeUser().toString() : null)
                .deviceId(jpaEntity.getDeviceId())
                .deviceName(jpaEntity.getDeviceName())
                .ipAddress(jpaEntity.getIpAddress())
                .userAgent(jpaEntity.getUserAgent())
                .reason(jpaEntity.getReason())
                .blockedBy(jpaEntity.getBlockedBy())
                .blockedAt(jpaEntity.getBlockedAt() != null ? jpaEntity.getBlockedAt().toString() : null)
                .expiresAt(jpaEntity.getExpiresAt())
                .build();
    }
}
