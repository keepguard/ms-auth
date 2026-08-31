package com.keepguard.ms_auth.application.dto.session;

import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantDeviceBlacklistView(
        UUID id,
        UUID companyId,
        String codeUser,
        String deviceId,
        String deviceName,
        String ipAddress,
        String userAgent,
        String reason,
        String blockedBy,
        String blockedAt,
        LocalDateTime expiresAt,
        Boolean writable
) {
    public static TenantDeviceBlacklistView from(DeviceBlacklistEntry entry, boolean writable) {
        if (entry == null) {
            return null;
        }
        return new TenantDeviceBlacklistView(
                entry.getId(),
                entry.getCompanyId(),
                entry.getCodeUser(),
                entry.getDeviceId(),
                entry.getDeviceName(),
                entry.getIpAddress(),
                entry.getUserAgent(),
                entry.getReason(),
                entry.getBlockedBy(),
                entry.getBlockedAt(),
                entry.getExpiresAt(),
                writable
        );
    }
}
