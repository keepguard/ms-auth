package com.keepguard.ms_auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CompanyRoleIdJpaEntity.class)
public class CompanyRoleJpaEntity {

    @Id
    @Column(name = "company_id", nullable = false, columnDefinition = "uuid")
    private UUID companyId;

    @Id
    @Column(name = "role_id", nullable = false, columnDefinition = "uuid")
    private UUID roleId;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean defaultRole = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
