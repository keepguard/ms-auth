package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceBlacklistRepositoryPort {

    DeviceBlacklistEntry save(DeviceBlacklistEntry entry);

    Optional<DeviceBlacklistEntry> findByCodeUserAndDeviceId(UUID codeUser, String deviceId);

    List<DeviceBlacklistEntry> listByCodeUser(UUID codeUser);

    List<DeviceBlacklistEntry> listByTenantIdAndCodeUser(UUID tenantId, UUID codeUser);

    void deleteByCodeUserAndDeviceId(UUID codeUser, String deviceId);

    boolean isBlacklisted(UUID codeUser, String deviceId);

    Page<DeviceBlacklistEntry> search(UUID tenantId, UUID codeUser, String deviceId, String deviceName, String ipAddress, java.time.LocalDateTime from, java.time.LocalDateTime to, Pageable pageable);
}
