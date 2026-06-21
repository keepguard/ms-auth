package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.user.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepositoryPort {
    UserRole save(UserRole userRole);
    Optional<UserRole> findById(UUID userId, UUID roleId);
    List<UserRole> findAll();
    void deleteById(UUID userId, UUID roleId);
    void delete(UserRole userRole);
    List<UserRole> findByUserId(UUID userId);
    List<UserRole> findByRoleId(UUID roleId);
    Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);
    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);
    void deleteByUserId(UUID userId);
}

