package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.DeviceBlacklistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceBlacklistSpringRepository extends JpaRepository<DeviceBlacklistJpaEntity, UUID>, JpaSpecificationExecutor<DeviceBlacklistJpaEntity> {

    Optional<DeviceBlacklistJpaEntity> findByCodeUserAndDeviceId(UUID codeUser, String deviceId);

    List<DeviceBlacklistJpaEntity> findByCodeUser(UUID codeUser);

    List<DeviceBlacklistJpaEntity> findByCompanyIdAndCodeUser(UUID companyId, UUID codeUser);

    void deleteByCodeUserAndDeviceId(UUID codeUser, String deviceId);

    boolean existsByCodeUserAndDeviceId(UUID codeUser, String deviceId);
}
