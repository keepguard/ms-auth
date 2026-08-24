package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.application.port.out.persistence.DeviceBlacklistRepositoryPort;
import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;
import com.keepguard.ms_auth.infrastructure.persistence.entity.DeviceBlacklistJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.DeviceBlacklistJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.DeviceBlacklistSpringRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
@Retry(name = "databaseOperation")
@Bulkhead(name = "databaseOperation")
public class DeviceBlacklistRepositoryAdapter implements DeviceBlacklistRepositoryPort {

    private final DeviceBlacklistSpringRepository springRepository;
    private final DeviceBlacklistJpaMapper mapper;

    @Override
    public DeviceBlacklistEntry save(DeviceBlacklistEntry entry) {
        DeviceBlacklistJpaEntity jpaEntity = mapper.toJpaEntity(entry);
        DeviceBlacklistJpaEntity saved = springRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DeviceBlacklistEntry> findByCodeUserAndDeviceId(UUID codeUser, String deviceId) {
        return springRepository.findByCodeUserAndDeviceId(codeUser, deviceId)
                .map(mapper::toDomain);
    }

    @Override
    public List<DeviceBlacklistEntry> listByCodeUser(UUID codeUser) {
        return springRepository.findByCodeUser(codeUser).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceBlacklistEntry> listByTenantIdAndCodeUser(UUID tenantId, UUID codeUser) {
        return springRepository.findByTenantIdAndCodeUser(tenantId, codeUser).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByCodeUserAndDeviceId(UUID codeUser, String deviceId) {
        springRepository.deleteByCodeUserAndDeviceId(codeUser, deviceId);
    }

    @Override
    public boolean isBlacklisted(UUID codeUser, String deviceId) {
        return springRepository.existsByCodeUserAndDeviceId(codeUser, deviceId);
    }
}
