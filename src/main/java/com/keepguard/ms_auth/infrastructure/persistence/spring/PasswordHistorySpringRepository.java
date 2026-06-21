package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.PasswordHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordHistorySpringRepository extends JpaRepository<PasswordHistoryJpaEntity, UUID> {
    List<PasswordHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
