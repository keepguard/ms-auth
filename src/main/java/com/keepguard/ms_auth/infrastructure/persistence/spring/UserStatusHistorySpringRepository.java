package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.UserStatusHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserStatusHistorySpringRepository extends JpaRepository<UserStatusHistoryJpaEntity, UUID> {
    List<UserStatusHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    void deleteByUserId(UUID userId);
}
