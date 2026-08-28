package com.keepguard.ms_auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_devices", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_device", columnNames = {"code_user", "device_id"})
}, indexes = {
        @Index(name = "idx_user_devices_user", columnList = "code_user"),
        @Index(name = "idx_user_devices_lookup", columnList = "code_user, device_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeviceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code_user", nullable = false, columnDefinition = "uuid")
    private UUID codeUser;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID companyId;

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "is_trusted", nullable = false)
    @Builder.Default
    private Boolean isTrusted = false;

    @Column(name = "first_seen_at")
    private LocalDateTime firstSeenAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.firstSeenAt == null) {
            this.firstSeenAt = now;
        }
        if (this.lastActiveAt == null) {
            this.lastActiveAt = now;
        }
    }
}
