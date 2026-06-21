package com.keepguard.ms_auth.test.builder;

import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.dto.role.*;
import com.keepguard.ms_auth.adapters.in.rest.role.dto.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder para criar objetos de teste da entidade Role
 * Seguindo o padrão usado no ms-company
 */
public class RoleTestBuilder {
    
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private UUID xApplication;
    
    private RoleTestBuilder() {
        // Valores padrão
        this.id = UUID.randomUUID();
        this.name = "ADMIN";
        this.description = "Administrator role";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
        this.xApplication = UUID.randomUUID();
    }
    
    public static RoleTestBuilder builder() {
        return new RoleTestBuilder();
    }
    
    public static RoleTestBuilder aRole() {
        return new RoleTestBuilder();
    }
    
    public RoleTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    
    public RoleTestBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public RoleTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public RoleTestBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public RoleTestBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public RoleTestBuilder withActive(Boolean active) {
        this.active = active;
        return this;
    }
    
    public RoleTestBuilder withXApplication(UUID xApplication) {
        this.xApplication = xApplication;
        return this;
    }
    
    public Role buildDomain() {
        return Role.builder()
            .id(id)
            .name(name)
            .description(description)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
    
    // ========== COMMAND DTO BUILDERS ==========
    
    public RoleCreateCommandDTO buildCreateCommand() {
        return RoleCreateCommandDTO.builder()
            .name(name)
            .description(description)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public RoleCreateCommandDTO buildCreateCommand(String name, String description) {
        return RoleCreateCommandDTO.builder()
            .name(name)
            .description(description)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public RoleUpdateCommandDTO buildUpdateCommand() {
        return RoleUpdateCommandDTO.builder()
            .id(id)
            .name(name)
            .description(description)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public RoleUpdateCommandDTO buildUpdateCommand(UUID id, String name, String description) {
        return RoleUpdateCommandDTO.builder()
            .id(id)
            .name(name)
            .description(description)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public RoleDeleteCommandDTO buildDeleteCommand() {
        return RoleDeleteCommandDTO.builder()
            .id(id)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public RoleDeleteCommandDTO buildDeleteCommand(UUID id) {
        return RoleDeleteCommandDTO.builder()
            .id(id)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    // ========== DTO BUILDERS (for mapper tests) ==========
    
    public RoleCreateDTO buildCreateDTO() {
        return RoleCreateDTO.builder()
            .name(name)
            .description(description)
            .build();
    }
    
    public RoleUpdateDTO buildUpdateDTO() {
        return RoleUpdateDTO.builder()
            .name(name)
            .description(description)
            .build();
    }
    
    public RoleResponseDTO buildResponseDTO() {
        return RoleResponseDTO.builder()
            .id(id)
            .name(name)
            .description(description)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
    
    // ========== LEGACY METHODS (for compatibility) ==========
    
    public Role asUser() {
        return buildDomain();
    }
}