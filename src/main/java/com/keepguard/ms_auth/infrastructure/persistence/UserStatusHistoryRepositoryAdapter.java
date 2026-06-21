package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import com.keepguard.ms_auth.application.port.out.persistence.UserStatusHistoryRepositoryPort;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserStatusHistoryJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.UserStatusHistoryJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.UserStatusHistorySpringRepository;
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
public class UserStatusHistoryRepositoryAdapter implements UserStatusHistoryRepositoryPort {

    private final UserStatusHistorySpringRepository springRepository;
    private final UserStatusHistoryJpaMapper mapper;

    @Override
    public UserStatusHistory save(UserStatusHistory userStatusHistory) {
        UserStatusHistoryJpaEntity jpaEntity = mapper.toJpaEntity(userStatusHistory);
        UserStatusHistoryJpaEntity savedEntity = springRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserStatusHistory> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<UserStatusHistory> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(UserStatusHistory userStatusHistory) {
        UserStatusHistoryJpaEntity jpaEntity = mapper.toJpaEntity(userStatusHistory);
        springRepository.delete(jpaEntity);
    }

    @Override
    public List<UserStatusHistory> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return springRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springRepository.deleteByUserId(userId);
    }
}
