package com.keepguard.ms_auth.domain.entity.role;

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
public class CompanyRole {
    private UUID companyId;
    private UUID roleId;
    private boolean enabled;
    private boolean defaultRole;
    private LocalDateTime createdAt;

    public static CompanyRole create(UUID companyId, UUID roleId, boolean enabled, boolean defaultRole) {
        return CompanyRole.builder()
                .companyId(companyId)
                .roleId(roleId)
                .enabled(enabled)
                .defaultRole(defaultRole)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
