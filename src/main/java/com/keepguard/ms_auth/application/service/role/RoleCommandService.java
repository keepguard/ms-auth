package com.keepguard.ms_auth.application.service.role;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.AuthorityRepositoryPort;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.dto.role.*;
import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.application.mapper.RoleApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleCommandService {

    private final RoleRepositoryPort roleRepository;
    private final AuthorityRepositoryPort authorityRepository;
    private final MetricsPort metricsPort;
    private final RoleApplicationMapper roleMapper;

    @LogOperation(
        operation = "CREATE_ROLE",
        description = "Criando nova role: {command.name}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "ROLE"
    )
    @Transactional
    public RoleCreateView create(RoleCreateCommandDTO command) {
        log.info("Creating role: {}", command.getName());

        if (roleRepository.findByName(command.getName()).isPresent()) {
            metricsPort.incrementCounter("role_business_errors_total",
                Map.of("error_type", "role_name_already_exists", "operation", "create"));
            throw new AlreadyExistsException("Role name already exists: " + command.getName());
        }

        Role role = Role.builder()
                .name(command.getName())
                .description(command.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Role savedRole = roleRepository.save(role);
        metricsPort.incrementCounter("role_created_total",
            Map.of("role_name", command.getName()));
        
        return roleMapper.toCreateView(savedRole);
    }

    @LogOperation(
        operation = "UPDATE_ROLE",
        description = "Atualizando role: {command.id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "ROLE"
    )
    @Transactional
    public RoleUpdateView update(RoleUpdateCommandDTO command) {
        log.info("Updating role with ID: {}", command.getId());

        Role existingRole = roleRepository.findById(command.getId())
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("role_business_errors_total",
                        Map.of("error_type", "role_not_found", "operation", "update"));
                    return new NotFoundException("Role not found with ID: " + command.getId());
                });

        if (!existingRole.getName().equals(command.getName()) &&
            roleRepository.findByName(command.getName()).isPresent()) {
            metricsPort.incrementCounter("role_business_errors_total",
                Map.of("error_type", "role_name_already_exists", "operation", "update"));
            throw new AlreadyExistsException("Role name already exists: " + command.getName());
        }

        existingRole.setName(command.getName());
        existingRole.setDescription(command.getDescription());
        existingRole.setUpdatedAt(LocalDateTime.now());

        Role updatedRole = roleRepository.save(existingRole);
        metricsPort.incrementCounter("role_updated_total",
            Map.of("role_id", command.getId().toString(), "role_name", command.getName()));
        
        return roleMapper.toUpdateView(updatedRole);
    }

    @LogOperation(
        operation = "DELETE_ROLE",
        description = "Removendo role: {command.id}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "ROLE"
    )
    @Transactional
    public void delete(RoleDeleteCommandDTO command) {
        log.info("Deleting role with ID: {}", command.getId());

        Role role = roleRepository.findById(command.getId())
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("role_business_errors_total",
                        Map.of("error_type", "role_not_found", "operation", "delete"));
                    return new NotFoundException("Role not found with ID: " + command.getId());
                });

        roleRepository.delete(role);
        metricsPort.incrementCounter("role_deleted_total",
            Map.of("role_id", command.getId().toString(), "role_name", role.getName()));
    }

    @LogOperation(
        operation = "ADD_AUTHORITY_TO_ROLE",
        description = "Adicionando authority {command.authorityName} ao role: {command.roleId}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "ROLE"
    )
    @Transactional
    public RoleAddAuthorityView addAuthority(RoleAddAuthorityCommandDTO command) {
        log.info("Adding authority {} to role: {}", command.getAuthorityName(), command.getRoleId());

        // Buscar o Role
        Role role = roleRepository.findById(command.getRoleId())
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("role_business_errors_total",
                        Map.of("error_type", "role_not_found", "operation", "add_authority"));
                    return new NotFoundException("Role not found with ID: " + command.getRoleId());
                });

        // Buscar a Authority
        Authority authority = authorityRepository.findByName(command.getAuthorityName())
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("role_business_errors_total",
                        Map.of("error_type", "authority_not_found", "operation", "add_authority"));
                    return new NotFoundException("Authority not found with name: " + command.getAuthorityName());
                });

        // Verificar se a authority já está associada (idempotente - não lança exceção)
        boolean alreadyExists = role.getAuthorities().stream()
                .anyMatch(auth -> auth.getId().equals(authority.getId()));

        if (alreadyExists) {
            log.info("Authority {} already exists in role: {}, returning current state", 
                    command.getAuthorityName(), command.getRoleId());
            metricsPort.incrementCounter("role_authority_already_exists_total",
                Map.of("role_id", command.getRoleId().toString(), "authority_name", command.getAuthorityName()));
        } else {
            // Adicionar a Authority ao Role
            role.getAuthorities().add(authority);
            role.setUpdatedAt(LocalDateTime.now());
            
            // Salvar (JPA atualiza automaticamente a tabela role_authorities)
            roleRepository.save(role);
            
            metricsPort.incrementCounter("role_authority_added_total",
                Map.of("role_id", command.getRoleId().toString(), "authority_name", command.getAuthorityName()));
        }

        return roleMapper.toAddAuthorityView(role, command.getAuthorityName());
    }

    @LogOperation(
        operation = "REMOVE_AUTHORITY_FROM_ROLE",
        description = "Removendo authority {command.authorityName} do role: {command.roleId}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "ROLE"
    )
    @Transactional
    public RoleRemoveAuthorityView removeAuthority(RoleRemoveAuthorityCommandDTO command) {
        log.info("Removing authority {} from role: {}", command.getAuthorityName(), command.getRoleId());

        // Buscar o Role
        Role role = roleRepository.findById(command.getRoleId())
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("role_business_errors_total",
                        Map.of("error_type", "role_not_found", "operation", "remove_authority"));
                    return new NotFoundException("Role not found with ID: " + command.getRoleId());
                });

        // Buscar a Authority
        Authority authority = authorityRepository.findByName(command.getAuthorityName())
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("role_business_errors_total",
                        Map.of("error_type", "authority_not_found", "operation", "remove_authority"));
                    return new NotFoundException("Authority not found with name: " + command.getAuthorityName());
                });

        // Verificar se a authority está associada ao role
        boolean exists = role.getAuthorities().stream()
                .anyMatch(auth -> auth.getId().equals(authority.getId()));

        if (!exists) {
            metricsPort.incrementCounter("role_business_errors_total",
                Map.of("error_type", "authority_not_associated", "operation", "remove_authority"));
            throw new NotFoundException("Authority " + command.getAuthorityName() + 
                    " is not associated with role ID: " + command.getRoleId());
        }

        // Remover a Authority do Role
        role.getAuthorities().removeIf(auth -> auth.getId().equals(authority.getId()));
        role.setUpdatedAt(LocalDateTime.now());

        // Salvar (JPA atualiza automaticamente a tabela role_authorities)
        roleRepository.save(role);

        metricsPort.incrementCounter("role_authority_removed_total",
            Map.of("role_id", command.getRoleId().toString(), "authority_name", command.getAuthorityName()));

        return roleMapper.toRemoveAuthorityView(role, command.getAuthorityName());
    }
}