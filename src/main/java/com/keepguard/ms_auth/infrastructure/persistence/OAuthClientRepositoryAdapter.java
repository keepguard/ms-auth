package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.application.port.out.persistence.OAuthClientRepositoryPort;
import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import com.keepguard.ms_auth.infrastructure.persistence.entity.OAuthClientJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.OAuthClientJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.OAuthClientSpringRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class OAuthClientRepositoryAdapter implements OAuthClientRepositoryPort {

    private final OAuthClientSpringRepository springRepository;
    private final OAuthClientJpaMapper mapper;

    @Override
    public OAuthClient save(OAuthClient client) {
        OAuthClientJpaEntity saved = springRepository.save(mapper.toJpaEntity(client));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OAuthClient> findByIdAndCompanyId(UUID id, UUID companyId) {
        return springRepository.findByIdAndCompanyId(id, companyId).map(mapper::toDomain);
    }

    @Override
    public Optional<OAuthClient> findByCompanyIdAndClientId(UUID companyId, String clientId) {
        return springRepository.findByCompanyIdAndClientId(companyId, clientId).map(mapper::toDomain);
    }

    @Override
    public List<OAuthClient> findAllByCompanyId(UUID companyId) {
        return springRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(OAuthClient client) {
        springRepository.delete(mapper.toJpaEntity(client));
    }
}
