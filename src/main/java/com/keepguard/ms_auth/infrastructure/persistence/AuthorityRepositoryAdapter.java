package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.application.port.out.persistence.AuthorityRepositoryPort;
import com.keepguard.ms_auth.infrastructure.persistence.entity.AuthorityJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.AuthorityJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.AuthoritySpringRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
@Retry(name = "databaseOperation")
@Bulkhead(name = "databaseOperation")
public class AuthorityRepositoryAdapter implements AuthorityRepositoryPort {

    private final AuthoritySpringRepository springRepository;
    private final AuthorityJpaMapper mapper;

    @Override
    public Authority save(Authority authority) {
        AuthorityJpaEntity jpaEntity = mapper.toJpaEntity(authority);
        AuthorityJpaEntity savedEntity = springRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Authority> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Authority> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Authority> findAll(Pageable pageable) {
        return springRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(Authority authority) {
        AuthorityJpaEntity jpaEntity = mapper.toJpaEntity(authority);
        springRepository.delete(jpaEntity);
    }

    @Override
    public Optional<Authority> findByName(String name) {
        return springRepository.findByName(name)
                .map(mapper::toDomain);
    }
}

