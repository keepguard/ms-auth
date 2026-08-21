package com.keepguard.ms_auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordHistoryJpaEntity {
    @Id

    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
