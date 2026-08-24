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
public class DeviceChallengeSession implements Serializable {
    private String challengeSessionId;
    private String codeUser;
    private String tenantId;
    private String username;
    private String email;
    private String phone;
    private String clientId;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String userAgent;
    private String activeCode;
    private String selectedChannel;
    private Integer attempts;
    private Integer maxAttempts;
    private Long expiresAt;
}
