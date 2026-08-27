package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.application.port.out.persistence.CompanyRoleRepositoryPort;
import com.keepguard.ms_auth.domain.entity.role.CompanyRole;
import com.keepguard.ms_auth.infrastructure.persistence.entity.CompanyRoleIdJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.CompanyRoleJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.CompanyRoleSpringRepository;
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
public class CompanyRoleRepositoryAdapter implements CompanyRoleRepositoryPort {

    private final CompanyRoleSpringRepository springRepository;
    private final CompanyRoleJpaMapper mapper;

    @Override
    public CompanyRole save(CompanyRole companyRole) {
        var saved = springRepository.save(mapper.toJpaEntity(companyRole));
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByCompanyId(UUID companyId) {
        return springRepository.existsByCompanyId(companyId);
    }

    @Override
    public Optional<CompanyRole> findByCompanyIdAndRoleId(UUID companyId, UUID roleId) {
        return springRepository.findByCompanyIdAndRoleId(companyId, roleId).map(mapper::toDomain);
    }

    @Override
    public List<CompanyRole> findEnabledDefaultsByCompanyId(UUID companyId) {
        return springRepository.findByCompanyIdAndEnabledTrueAndDefaultRoleTrue(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanyRole> findByCompanyId(UUID companyId) {
        return springRepository.findByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByCompanyIdAndRoleId(UUID companyId, UUID roleId) {
        springRepository.deleteByCompanyIdAndRoleId(companyId, roleId);
    }

    public void deleteById(UUID companyId, UUID roleId) {
        springRepository.deleteById(new CompanyRoleIdJpaEntity(companyId, roleId));
    }
}
