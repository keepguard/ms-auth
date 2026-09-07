package com.keepguard.ms_auth.adapters.in.rest.session.dto.response;

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
public class DeviceBlacklistResponseDTO {
    private UUID id;
    private UUID companyId;
    private String codeUser;
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private String userAgent;
    private String reason;
    private String blockedBy;
    private String blockedAt;
    private LocalDateTime expiresAt;
    private Boolean writable;
}
