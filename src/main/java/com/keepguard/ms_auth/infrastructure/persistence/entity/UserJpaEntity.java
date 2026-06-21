package com.keepguard.ms_auth.infrastructure.persistence.entity;

import com.keepguard.ms_auth.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "id_user_external", nullable = false)
    private UUID idUserExternal;

    @Column(name = "code_user", nullable = false)
    private UUID codeUser;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "company_id", nullable = false, columnDefinition = "uuid")
    private UUID companyId;

    @Column(name = "company_code", nullable = false, columnDefinition = "uuid")
    private UUID companyCode;

    @Column(name = "x_application", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID xApplication;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
