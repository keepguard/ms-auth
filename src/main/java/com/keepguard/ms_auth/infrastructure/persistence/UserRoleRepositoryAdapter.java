package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.domain.entity.user.UserRole;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.UserRoleJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.UserRoleSpringRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Retry(name = "databaseOperation")
@Bulkhead(name = "databaseOperation")
public class UserRoleRepositoryAdapter implements UserRoleRepositoryPort {

    private final UserRoleSpringRepository springRepository;
    private final UserRoleJpaMapper mapper;

    @Override
    public UserRole save(UserRole userRole) {
        UserRoleJpaEntity jpaEntity = mapper.toJpaEntity(userRole);
        UserRoleJpaEntity savedEntity = springRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserRole> findById(UUID userId, UUID roleId) {
        return springRepository.findById(new com.keepguard.ms_auth.infrastructure.persistence.entity.UserRoleIdJpaEntity(userId, roleId))
                .map(mapper::toDomain);
    }

    @Override
    public Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId) {
        return springRepository.findByUserId(userId).stream()
                .filter(ur -> ur.getRoleId().equals(roleId))
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public List<UserRole> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID userId, UUID roleId) {
        springRepository.deleteById(new com.keepguard.ms_auth.infrastructure.persistence.entity.UserRoleIdJpaEntity(userId, roleId));
    }

    @Override
    public void delete(UserRole userRole) {
        UserRoleJpaEntity jpaEntity = mapper.toJpaEntity(userRole);
        springRepository.delete(jpaEntity);
    }

    @Override
    public List<UserRole> findByUserId(UUID userId) {
        return springRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserRole> findByRoleId(UUID roleId) {
        return springRepository.findByRoleId(roleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByUserIdAndRoleId(UUID userId, UUID roleId) {
        springRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springRepository.deleteByUserId(userId);
    }
}
