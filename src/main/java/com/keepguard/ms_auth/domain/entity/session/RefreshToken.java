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
public class RefreshToken implements Serializable {
    private String tokenHash;
    private String sid;
    private String codeUser;
    private String companyId;
    private String clientId;
    private String deviceId;
    private String familyId;
    private String createdAt;
    private String absoluteExpiresAt;
    private String replacedByHash;
    private boolean revoked;
}
