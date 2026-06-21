package com.keepguard.ms_auth.domain.entity.user;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.keepguard.ms_auth.domain.enums.UserStatusEventType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusHistory {
    private UUID id;
    private UUID userId;
    private UserStatusEventType eventType;
    private String reason;
    private LocalDateTime createdAt;

    public static UserStatusHistory create(UUID userId, UserStatusEventType eventType, String reason) {
        return UserStatusHistory.builder()
                .userId(userId)
                .eventType(eventType)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
    }
}