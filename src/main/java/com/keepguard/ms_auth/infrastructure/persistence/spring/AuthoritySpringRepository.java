package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.AuthorityJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthoritySpringRepository extends JpaRepository<AuthorityJpaEntity, UUID> {
    Optional<AuthorityJpaEntity> findByName(String name);
    
    Page<AuthorityJpaEntity> findAll(Pageable pageable);
}

