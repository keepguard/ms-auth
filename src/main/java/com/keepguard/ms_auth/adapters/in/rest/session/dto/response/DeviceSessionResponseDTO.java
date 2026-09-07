package com.keepguard.ms_auth.adapters.in.rest.session.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSessionResponseDTO {
    private String sessionId;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String location;
    private Boolean isCurrent;
    private Boolean isTrusted;
    private String lastActiveAt;
    private String createdAt;
    private String codeUser;
    private Boolean writable;
}
