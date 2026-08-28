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
public class UserSession implements Serializable {
    private String sessionId;
    private String codeUser;
    private String companyId;
    private String clientId;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String location;
    private String userAgent;
    private Boolean isTrusted;
    private String lastActiveAt;
    private String createdAt;
    private String accessTokenHash;
    private String refreshToken;
}
