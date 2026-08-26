package com.keepguard.ms_auth.infrastructure.persistence.specification;

import com.keepguard.ms_auth.infrastructure.persistence.entity.DeviceBlacklistJpaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public final class DeviceBlacklistSpecifications {

    private DeviceBlacklistSpecifications() {}

    public static Specification<DeviceBlacklistJpaEntity> withTenantId(UUID tenantId) {
        return (root, query, cb) -> tenantId == null ? cb.conjunction() : cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<DeviceBlacklistJpaEntity> withCodeUser(UUID codeUser) {
        return (root, query, cb) -> codeUser == null ? cb.conjunction() : cb.equal(root.get("codeUser"), codeUser);
    }

    public static Specification<DeviceBlacklistJpaEntity> withDeviceId(String deviceId) {
        return (root, query, cb) -> (deviceId == null || deviceId.isBlank()) 
                ? cb.conjunction() 
                : cb.like(cb.lower(root.get("deviceId")), "%" + deviceId.trim().toLowerCase() + "%");
    }

    public static Specification<DeviceBlacklistJpaEntity> withDeviceName(String deviceName) {
        return (root, query, cb) -> (deviceName == null || deviceName.isBlank()) 
                ? cb.conjunction() 
                : cb.like(cb.lower(root.get("deviceName")), "%" + deviceName.trim().toLowerCase() + "%");
    }

    public static Specification<DeviceBlacklistJpaEntity> withIpAddress(String ipAddress) {
        return (root, query, cb) -> (ipAddress == null || ipAddress.isBlank()) 
                ? cb.conjunction() 
                : cb.equal(root.get("ipAddress"), ipAddress.trim());
    }

    public static Specification<DeviceBlacklistJpaEntity> withBlockedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("blockedAt"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("blockedAt"), from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(root.get("blockedAt"), to);
            }
            return cb.conjunction();
        };
    }
}
