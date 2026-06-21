package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.domain.entity.user.PasswordHistory;
import com.keepguard.ms_auth.application.port.out.persistence.PasswordHistoryRepositoryPort;
import com.keepguard.ms_auth.infrastructure.persistence.entity.PasswordHistoryJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.PasswordHistoryJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.PasswordHistorySpringRepository;
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
public class PasswordHistoryRepositoryAdapter implements PasswordHistoryRepositoryPort {

    private final PasswordHistorySpringRepository springRepository;
    private final PasswordHistoryJpaMapper mapper;

    @Override
    public PasswordHistory save(PasswordHistory passwordHistory) {
        PasswordHistoryJpaEntity jpaEntity = mapper.toJpaEntity(passwordHistory);
        PasswordHistoryJpaEntity savedEntity = springRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PasswordHistory> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<PasswordHistory> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(PasswordHistory passwordHistory) {
        PasswordHistoryJpaEntity jpaEntity = mapper.toJpaEntity(passwordHistory);
        springRepository.delete(jpaEntity);
    }

    @Override
    public List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return springRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(UUID userId) {
        return springRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(5)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
