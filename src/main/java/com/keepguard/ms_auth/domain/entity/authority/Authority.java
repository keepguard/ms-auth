package com.keepguard.ms_auth.domain.entity.authority;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authority {
    private UUID id;
    private UUID companyId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean belongsToCompany(UUID companyId) {
        return companyId != null && companyId.equals(this.companyId);
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

