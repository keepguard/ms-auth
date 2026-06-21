package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleSpringRepository extends JpaRepository<RoleJpaEntity, UUID> {
    Optional<RoleJpaEntity> findByName(String name);
}
