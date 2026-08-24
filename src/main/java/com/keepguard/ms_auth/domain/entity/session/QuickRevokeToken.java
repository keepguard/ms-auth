package com.keepguard.ms_auth.domain.entity.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickRevokeToken implements Serializable {
    private String token;
    private String codeUser;
    private String tenantId;
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private String userAgent;
    private String createdAt;
    private Long expiresAt;
}
