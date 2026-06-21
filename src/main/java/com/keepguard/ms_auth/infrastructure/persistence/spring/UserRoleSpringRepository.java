package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserRoleIdJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleSpringRepository extends JpaRepository<UserRoleJpaEntity, UserRoleIdJpaEntity> {
    List<UserRoleJpaEntity> findByUserId(UUID userId);
    List<UserRoleJpaEntity> findByRoleId(UUID roleId);
    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);
    void deleteByUserId(UUID userId);
}
