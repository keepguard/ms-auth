package com.keepguard.ms_auth.application.service.authority;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.application.port.out.persistence.AuthorityRepositoryPort;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.dto.authority.*;
import com.keepguard.ms_auth.application.dto.authority.AuthorityCreateView;
import com.keepguard.ms_auth.application.dto.authority.AuthorityUpdateView;
import com.keepguard.ms_auth.application.mapper.AuthorityApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorityCommandService {

    private final AuthorityRepositoryPort authorityRepository;
    private final MetricsPort metricsPort;
    private final AuthorityApplicationMapper authorityMapper;

    @LogOperation(
        operation = "CREATE_AUTHORITY",
        description = "Criando nova authority: {command.name}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "AUTHORITY"
    )
    @Transactional
    public AuthorityCreateView create(AuthorityCreateCommandDTO command) {
        log.info("Creating authority: {}", command.getName());

        if (authorityRepository.findByName(command.getName()).isPresent()) {
            metricsPort.incrementCounter("authority_business_errors_total",
                Map.of("error_type", "authority_name_already_exists", "operation", "create"));
            throw new AlreadyExistsException("Authority name already exists: " + command.getName());
        }

        Authority authority = Authority.builder()
                .name(command.getName())
                .description(command.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Authority savedAuthority = authorityRepository.save(authority);
        metricsPort.incrementCounter("authority_created_total",
            Map.of("authority_name", command.getName()));
        
        return authorityMapper.toCreateView(savedAuthority);
    }

    @LogOperation(
        operation = "UPDATE_AUTHORITY",
        description = "Atualizando authority: {command.id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "AUTHORITY"
    )
    @Transactional
    public AuthorityUpdateView update(AuthorityUpdateCommandDTO command) {
        log.info("Updating authority with ID: {}", command.getId());

        Authority existingAuthority = authorityRepository.findById(command.getId())
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("authority_business_errors_total",
                        Map.of("error_type", "authority_not_found", "operation", "update"));
                    return new NotFoundException("Authority not found with ID: " + command.getId());
                });

        if (!existingAuthority.getName().equals(command.getName()) &&
            authorityRepository.findByName(command.getName()).isPresent()) {
            metricsPort.incrementCounter("authority_business_errors_total",
                Map.of("error_type", "authority_name_already_exists", "operation", "update"));
            throw new AlreadyExistsException("Authority name already exists: " + command.getName());
        }

        existingAuthority.setName(command.getName());
        existingAuthority.setDescription(command.getDescription());
        existingAuthority.setUpdatedAt(LocalDateTime.now());

        Authority updatedAuthority = authorityRepository.save(existingAuthority);
        metricsPort.incrementCounter("authority_updated_total",
            Map.of("authority_id", command.getId().toString(), "authority_name", command.getName()));
        
        return authorityMapper.toUpdateView(updatedAuthority);
    }

    @LogOperation(
        operation = "DELETE_AUTHORITY",
        description = "Removendo authority: {command.id}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "AUTHORITY"
    )
    @Transactional
    public void delete(AuthorityDeleteCommandDTO command) {
        log.info("Deleting authority with ID: {}", command.getId());

        Authority authority = authorityRepository.findById(command.getId())
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("authority_business_errors_total",
                        Map.of("error_type", "authority_not_found", "operation", "delete"));
                    return new NotFoundException("Authority not found with ID: " + command.getId());
                });

        authorityRepository.delete(authority);
        metricsPort.incrementCounter("authority_deleted_total",
            Map.of("authority_id", command.getId().toString(), "authority_name", authority.getName()));
    }
}

