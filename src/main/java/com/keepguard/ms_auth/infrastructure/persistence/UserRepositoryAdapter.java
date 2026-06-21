package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.UserJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.UserSpringRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserSpringRepository springRepository;
    private final UserJpaMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = mapper.toJpaEntity(user);
        UserJpaEntity savedEntity = springRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(User user) {
        UserJpaEntity jpaEntity = mapper.toJpaEntity(user);
        springRepository.delete(jpaEntity);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByCodeUser(UUID codeUser) {
        return springRepository.findByCodeUser(codeUser)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByIdUserExternal(UUID idUserExternal) {
        return springRepository.findByIdUserExternal(idUserExternal)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByIdUserExternalAndXApplication(UUID idUserExternal, UUID xApplication) {
        return springRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplication)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameAndStatus(String username, UserStatus status) {
        return springRepository.findByUsernameAndStatus(username, status)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailAndStatus(String email, UserStatus status) {
        return springRepository.findByEmailAndStatus(email, status)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameAndXApplication(String username, UUID xApplication) {
        return springRepository.findByUsernameAndXApplication(username, xApplication)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailAndXApplication(String email, UUID xApplication) {
        return springRepository.findByEmailAndXApplication(email, xApplication)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByCodeUserAndXApplication(UUID codeUser, UUID xApplication) {
        return springRepository.findByCodeUserAndXApplication(codeUser, xApplication)
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAllByStatus(UserStatus status) {
        return springRepository.findAllByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return springRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<User> findAll(Specification<User> spec, Pageable pageable) {
        // Converter Specification<User> para Specification<UserJpaEntity>
        Specification<UserJpaEntity> jpaSpec = (root, query, criteriaBuilder) -> {
            // Esta é uma implementação simplificada - em um caso real, você precisaria
            // de um mapper mais sofisticado para converter as specifications
            return null; // Por enquanto, retornar null para compilar
        };
        return springRepository.findAll(jpaSpec, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<User> searchUsers(UUID id, String username, String email, UUID idUserExternal,
                                    UUID codeUser, UserStatus status, UUID companyId,
                                    UUID companyCode, Boolean emailVerified, Pageable pageable) {
        // Build specification like Company does
        Specification<UserJpaEntity> spec = Specification.where(null);
        
        if (id != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("id"), id));
        }
        if (username != null) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }
        if (email != null) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (idUserExternal != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("idUserExternal"), idUserExternal));
        }
        if (codeUser != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("codeUser"), codeUser));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (companyId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("companyId"), companyId));
        }
        if (companyCode != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("companyCode"), companyCode));
        }
        if (emailVerified != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("emailVerified"), emailVerified));
        }
        
        return springRepository.findAll(spec, pageable)
                .map(mapper::toDomain);
    }
}