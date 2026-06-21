package com.keepguard.ms_auth.infrastructure.persistence.entity;

import com.keepguard.ms_auth.domain.enums.UserStatusEventType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusHistoryJpaEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatusEventType eventType;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
