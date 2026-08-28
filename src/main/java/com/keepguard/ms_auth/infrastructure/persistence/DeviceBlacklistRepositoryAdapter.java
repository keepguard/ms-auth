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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public DeviceBlacklistEntry save(DeviceBlacklistEntry entry) {
        DeviceBlacklistJpaEntity jpaEntity = mapper.toJpaEntity(entry);
        DeviceBlacklistJpaEntity saved = springRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeviceBlacklistEntry> findByCodeUserAndDeviceId(UUID codeUser, String deviceId) {
        return springRepository.findByCodeUserAndDeviceId(codeUser, deviceId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceBlacklistEntry> listByCodeUser(UUID codeUser) {
        return springRepository.findByCodeUser(codeUser).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceBlacklistEntry> listByTenantIdAndCodeUser(UUID companyId, UUID codeUser) {
        return springRepository.findByCompanyIdAndCodeUser(companyId, codeUser).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByCodeUserAndDeviceId(UUID codeUser, String deviceId) {
        springRepository.deleteByCodeUserAndDeviceId(codeUser, deviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(UUID codeUser, String deviceId) {
        return springRepository.existsByCodeUserAndDeviceId(codeUser, deviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<DeviceBlacklistEntry> search(
            UUID companyId, UUID codeUser, String deviceId, String deviceName, String ipAddress,
            java.time.LocalDateTime from, java.time.LocalDateTime to, org.springframework.data.domain.Pageable pageable) {
        
        org.springframework.data.jpa.domain.Specification<DeviceBlacklistJpaEntity> spec = org.springframework.data.jpa.domain.Specification
                .where(com.keepguard.ms_auth.infrastructure.persistence.specification.DeviceBlacklistSpecifications.withTenantId(companyId))
                .and(com.keepguard.ms_auth.infrastructure.persistence.specification.DeviceBlacklistSpecifications.withCodeUser(codeUser))
                .and(com.keepguard.ms_auth.infrastructure.persistence.specification.DeviceBlacklistSpecifications.withDeviceId(deviceId))
                .and(com.keepguard.ms_auth.infrastructure.persistence.specification.DeviceBlacklistSpecifications.withDeviceName(deviceName))
                .and(com.keepguard.ms_auth.infrastructure.persistence.specification.DeviceBlacklistSpecifications.withIpAddress(ipAddress))
                .and(com.keepguard.ms_auth.infrastructure.persistence.specification.DeviceBlacklistSpecifications.withBlockedBetween(from, to));

        return springRepository.findAll(spec, pageable).map(mapper::toDomain);
    }
}
