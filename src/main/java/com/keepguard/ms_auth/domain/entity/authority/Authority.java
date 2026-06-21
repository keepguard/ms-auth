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
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

