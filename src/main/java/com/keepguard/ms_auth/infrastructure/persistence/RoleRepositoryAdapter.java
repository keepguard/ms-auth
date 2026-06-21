package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.infrastructure.persistence.entity.AuthorityJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.RoleJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.AuthoritySpringRepository;
import com.keepguard.ms_auth.infrastructure.persistence.spring.RoleSpringRepository;
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
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleSpringRepository springRepository;
    private final AuthoritySpringRepository authoritySpringRepository;
    private final RoleJpaMapper mapper;

    @Override
    public Role save(Role role) {
        RoleJpaEntity jpaEntity;
        
        // Se o role já existe, buscar a entidade existente e atualizar
        if (role.getId() != null) {
            jpaEntity = springRepository.findById(role.getId())
                    .orElse(mapper.toJpaEntity(role));
            
            // Atualizar campos
            jpaEntity.setName(role.getName());
            jpaEntity.setDescription(role.getDescription());
            jpaEntity.setUpdatedAt(role.getUpdatedAt());
            
            // Atualizar authorities se existirem
            if (role.getAuthorities() != null) {
                jpaEntity.getAuthorities().clear();
                role.getAuthorities().forEach(authority -> {
                    AuthorityJpaEntity authorityJpa = authoritySpringRepository.findById(authority.getId())
                            .orElseThrow(() -> new RuntimeException("Authority not found: " + authority.getId()));
                    jpaEntity.getAuthorities().add(authorityJpa);
                });
            }
        } else {
            jpaEntity = mapper.toJpaEntity(role);
        }
        
        RoleJpaEntity savedEntity = springRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Role> findAll(Pageable pageable) {
        return springRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(Role role) {
        RoleJpaEntity jpaEntity = mapper.toJpaEntity(role);
        springRepository.delete(jpaEntity);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return springRepository.findByName(name)
                .map(mapper::toDomain);
    }
}