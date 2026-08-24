package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.application.port.out.persistence.UserDeviceRepositoryPort;
import com.keepguard.ms_auth.domain.entity.session.UserDevice;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserDeviceJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.UserDeviceJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.UserDeviceSpringRepository;
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
public class UserDeviceRepositoryAdapter implements UserDeviceRepositoryPort {

    private final UserDeviceSpringRepository springRepository;
    private final UserDeviceJpaMapper mapper;

    @Override
    public UserDevice save(UserDevice device) {
        UserDeviceJpaEntity jpaEntity = mapper.toJpaEntity(device);
        UserDeviceJpaEntity saved = springRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UserDevice> findByCodeUserAndDeviceId(UUID codeUser, String deviceId) {
        return springRepository.findByCodeUserAndDeviceId(codeUser, deviceId)
                .map(mapper::toDomain);
    }

    @Override
    public List<UserDevice> listByCodeUser(UUID codeUser) {
        return springRepository.findByCodeUser(codeUser).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDevice> listByCodeUserAndTenantId(UUID codeUser, UUID tenantId) {
        return springRepository.findByCodeUserAndTenantId(codeUser, tenantId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByCodeUserAndDeviceId(UUID codeUser, String deviceId) {
        springRepository.deleteByCodeUserAndDeviceId(codeUser, deviceId);
    }
}
