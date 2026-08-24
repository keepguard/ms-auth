package com.keepguard.ms_auth.domain.entity.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice {
    private UUID id;
    private UUID codeUser;
    private UUID tenantId;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String userAgent;
    @Builder.Default
    private Boolean isTrusted = false;
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime revokedAt;

    public void updateActivity(String ipAddress, String userAgent, LocalDateTime activeAt) {
        if (ipAddress != null && !ipAddress.isBlank()) {
            this.ipAddress = ipAddress;
        }
        if (userAgent != null && !userAgent.isBlank()) {
            this.userAgent = userAgent;
        }
        this.lastActiveAt = activeAt != null ? activeAt : LocalDateTime.now();
    }

    public void revoke(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt != null ? revokedAt : LocalDateTime.now();
    }
}
