package com.keepguard.ms_auth.domain.entity.user;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordHistory {
    private UUID id;
    private UUID userId;
    private String passwordHash;
    private LocalDateTime createdAt;
}