package com.keepguard.ms_auth.domain.entity.role;

import com.keepguard.ms_auth.domain.entity.authority.Authority;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    private UUID id;
    private UUID companyId;
    private String name;
    private String description;
    private boolean isSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private Set<Authority> authorities = new HashSet<>();

    public boolean belongsToCompany(UUID companyId) {
        return companyId != null && companyId.equals(this.companyId);
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}