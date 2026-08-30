package com.keepguard.ms_auth.infrastructure.persistence.entity;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "oauth_clients",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_oauth_clients_company_client",
                columnNames = {"company_id", "client_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthClientJpaEntity {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false, columnDefinition = "uuid")
    private UUID companyId;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "secret_hash", nullable = false, length = 255)
    private String secretHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth_client_authorities", joinColumns = @JoinColumn(name = "oauth_client_id"))
    @Column(name = "authority", nullable = false, length = 80)
    @Builder.Default
    private List<String> authorities = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OAuthClientStatus status = OAuthClientStatus.ACTIVE;

    @Column(name = "token_ttl_seconds", nullable = false)
    private int tokenTtlSeconds;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
