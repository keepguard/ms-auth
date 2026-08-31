package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.UserDeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceSpringRepository extends JpaRepository<UserDeviceJpaEntity, UUID>, JpaSpecificationExecutor<UserDeviceJpaEntity> {

    Optional<UserDeviceJpaEntity> findByCodeUserAndDeviceId(UUID codeUser, String deviceId);

    List<UserDeviceJpaEntity> findByCodeUser(UUID codeUser);

    List<UserDeviceJpaEntity> findByCodeUserAndCompanyId(UUID codeUser, UUID companyId);

    void deleteByCodeUserAndDeviceId(UUID codeUser, String deviceId);
}
