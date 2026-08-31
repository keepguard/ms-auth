package com.keepguard.ms_auth.domain.entity.oauth;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthClient {
    private UUID id;
    private UUID companyId;
    private String clientId;
    private String secretHash;
    private UUID serviceRoleId;
    private String serviceRoleName;
    @Builder.Default
    private List<String> authorities = new ArrayList<>();
    @Builder.Default
    private OAuthClientStatus status = OAuthClientStatus.ACTIVE;
    private int tokenTtlSeconds;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status == OAuthClientStatus.ACTIVE;
    }

    public void block() {
        this.status = OAuthClientStatus.BLOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void unblock() {
        this.status = OAuthClientStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
}
