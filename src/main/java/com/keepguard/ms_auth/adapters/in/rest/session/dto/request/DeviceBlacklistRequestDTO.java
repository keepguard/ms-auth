package com.keepguard.ms_auth.adapters.in.rest.session.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceBlacklistRequestDTO {
    private String userId;
    private String deviceId;
    private String deviceName;
    private String reason;
    private String expiresAt;
}
