package com.keepguard.ms_auth.domain.entity.user;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    private UUID userId;
    private UUID roleId;
    private LocalDateTime assignedAt;

    public static UserRole assign(UUID userId, UUID roleId) {
        return UserRole.builder()
                .userId(userId)
                .roleId(roleId)
                .assignedAt(LocalDateTime.now())
                .build();
    }
}