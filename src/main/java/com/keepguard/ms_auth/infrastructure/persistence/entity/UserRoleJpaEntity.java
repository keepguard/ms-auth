package com.keepguard.ms_auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserRoleIdJpaEntity.class)
public class UserRoleJpaEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}
