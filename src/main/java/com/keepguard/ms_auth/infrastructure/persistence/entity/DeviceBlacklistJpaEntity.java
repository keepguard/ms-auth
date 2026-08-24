package com.keepguard.ms_auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_blacklist", uniqueConstraints = {
        @UniqueConstraint(name = "uk_device_blacklist", columnNames = {"code_user", "device_id"})
}, indexes = {
        @Index(name = "idx_device_blacklist_lookup", columnList = "code_user, device_id"),
        @Index(name = "idx_device_blacklist_tenant", columnList = "tenant_id, code_user")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceBlacklistJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "code_user", nullable = false, columnDefinition = "uuid")
    private UUID codeUser;

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "blocked_by", length = 100)
    private String blockedBy;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        if (this.blockedAt == null) {
            this.blockedAt = LocalDateTime.now();
        }
    }
}
