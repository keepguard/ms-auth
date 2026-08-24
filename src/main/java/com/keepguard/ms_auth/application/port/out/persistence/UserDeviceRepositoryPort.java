package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.session.UserDevice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepositoryPort {

    UserDevice save(UserDevice device);

    Optional<UserDevice> findByCodeUserAndDeviceId(UUID codeUser, String deviceId);

    List<UserDevice> listByCodeUser(UUID codeUser);

    List<UserDevice> listByCodeUserAndTenantId(UUID codeUser, UUID tenantId);

    void deleteByCodeUserAndDeviceId(UUID codeUser, String deviceId);
}
