package com.keepguard.ms_auth.adapters.in.rest.role.mapper;

import com.keepguard.ms_auth.adapters.in.rest.role.dto.*;
import com.keepguard.ms_auth.domain.dto.role.*;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.application.dto.role.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RoleAdapterMapper {

    public Role toEntity(RoleCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        return Role.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    public Role toEntity(RoleUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        return Role.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    public Role toEntity(RoleCreateCommandDTO command) {
        if (command == null) {
            return null;
        }

        return Role.builder()
                .name(command.getName())
                .description(command.getDescription())
                .build();
    }

    public Role toEntity(RoleUpdateCommandDTO command) {
        if (command == null) {
            return null;
        }

        return Role.builder()
                .name(command.getName())
                .description(command.getDescription())
                .build();
    }

    public RoleResponseDTO toResponseDTO(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    // Conversões internas movidas para RoleApplicationMapper

    // ========== VIEW TO RESPONSE DTO CONVERSIONS ==========

    public RoleResponseDTO toResponseDTO(RoleCreateView view) {
        if (view == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public RoleResponseDTO toResponseDTO(RoleUpdateView view) {
        if (view == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public RoleResponseDTO toResponseDTO(RoleGetByIdView view) {
        if (view == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public RoleResponseDTO toResponseDTO(RoleGetByNameView view) {
        if (view == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public RoleResponseDTO toResponseDTO(RoleListView view) {
        if (view == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public RoleResponseDTO toResponseDTO(RoleSearchView view) {
        if (view == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    // ========== SPECIFIC RESPONSE DTO CONVERSIONS ==========

    public RoleCreateResponseDTO toCreateResponseDTO(RoleResponseDTO roleResponse) {
        if (roleResponse == null) {
            return null;
        }

        return RoleCreateResponseDTO.builder()
                .id(roleResponse.getId())
                .name(roleResponse.getName())
                .description(roleResponse.getDescription())
                .createdAt(roleResponse.getCreatedAt())
                .updatedAt(roleResponse.getUpdatedAt())
                .active(true) // Default value since RoleResponseDTO doesn't have active field
                .build();
    }

    public RoleUpdateResponseDTO toUpdateResponseDTO(RoleResponseDTO roleResponse) {
        if (roleResponse == null) {
            return null;
        }

        return RoleUpdateResponseDTO.builder()
                .id(roleResponse.getId())
                .name(roleResponse.getName())
                .description(roleResponse.getDescription())
                .createdAt(roleResponse.getCreatedAt())
                .updatedAt(roleResponse.getUpdatedAt())
                .active(true) // Default value since RoleResponseDTO doesn't have active field
                .build();
    }

    public RoleGetByIdResponseDTO toGetByIdResponseDTO(RoleResponseDTO roleResponse) {
        if (roleResponse == null) {
            return null;
        }

        return RoleGetByIdResponseDTO.builder()
                .id(roleResponse.getId())
                .name(roleResponse.getName())
                .description(roleResponse.getDescription())
                .createdAt(roleResponse.getCreatedAt())
                .updatedAt(roleResponse.getUpdatedAt())
                .active(true) // Default value since RoleResponseDTO doesn't have active field
                .build();
    }

    public RoleGetByNameResponseDTO toGetByNameResponseDTO(RoleResponseDTO roleResponse) {
        if (roleResponse == null) {
            return null;
        }

        return RoleGetByNameResponseDTO.builder()
                .id(roleResponse.getId())
                .name(roleResponse.getName())
                .description(roleResponse.getDescription())
                .createdAt(roleResponse.getCreatedAt())
                .updatedAt(roleResponse.getUpdatedAt())
                .active(true) // Default value since RoleResponseDTO doesn't have active field
                .build();
    }

    public RoleListResponseDTO toListResponseDTO(RoleResponseDTO roleResponse) {
        if (roleResponse == null) {
            return null;
        }

        return RoleListResponseDTO.builder()
                .id(roleResponse.getId())
                .name(roleResponse.getName())
                .description(roleResponse.getDescription())
                .createdAt(roleResponse.getCreatedAt())
                .updatedAt(roleResponse.getUpdatedAt())
                .active(true) // Default value since RoleResponseDTO doesn't have active field
                .build();
    }

    public RoleSearchResponseDTO toSearchResponseDTO(RoleResponseDTO roleResponse) {
        if (roleResponse == null) {
            return null;
        }

        return RoleSearchResponseDTO.builder()
                .id(roleResponse.getId())
                .name(roleResponse.getName())
                .description(roleResponse.getDescription())
                .createdAt(roleResponse.getCreatedAt())
                .updatedAt(roleResponse.getUpdatedAt())
                .active(true) // Default value since RoleResponseDTO doesn't have active field
                .build();
    }

    // ========== VIEW TO SPECIFIC RESPONSE DTO CONVERSIONS ==========

    public RoleCreateResponseDTO toCreateResponseDTO(RoleCreateView view) {
        if (view == null) {
            return null;
        }

        return RoleCreateResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .active(true) // Default value
                .build();
    }

    public RoleUpdateResponseDTO toUpdateResponseDTO(RoleUpdateView view) {
        if (view == null) {
            return null;
        }

        return RoleUpdateResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .active(true) // Default value
                .build();
    }

    public RoleGetByIdResponseDTO toGetByIdResponseDTO(RoleGetByIdView view) {
        if (view == null) {
            return null;
        }

        return RoleGetByIdResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .active(true) // Default value
                .build();
    }

    public RoleGetByNameResponseDTO toGetByNameResponseDTO(RoleGetByNameView view) {
        if (view == null) {
            return null;
        }

        return RoleGetByNameResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .active(true) // Default value
                .build();
    }

    public RoleListResponseDTO toListResponseDTO(RoleListView view) {
        if (view == null) {
            return null;
        }

        return RoleListResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .active(true) // Default value
                .build();
    }

    public RoleSearchResponseDTO toSearchResponseDTO(RoleSearchView view) {
        if (view == null) {
            return null;
        }

        return RoleSearchResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .active(true) // Default value
                .build();
    }

    // ========== COMMAND DTO CONVERSIONS ==========

    public RoleCreateCommandDTO toCreateCommand(RoleCreateDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }
        try {
            return RoleCreateCommandDTO.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear RoleCreateDTO para RoleCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RoleUpdateCommandDTO toUpdateCommand(UUID id, RoleUpdateDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }
        try {
            return RoleUpdateCommandDTO.builder()
                    .id(id)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear RoleUpdateDTO para RoleUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RoleDeleteCommandDTO toDeleteCommand(UUID id, UUID xApplicationUuid) {
        try {
            return RoleDeleteCommandDTO.builder()
                    .id(id)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar RoleDeleteCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RoleGetByIdQueryDTO toGetByIdCommand(UUID id, UUID xApplicationUuid) {
        try {
            return RoleGetByIdQueryDTO.builder()
                    .id(id)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar RoleGetByIdQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RoleGetByNameQueryDTO toGetByNameCommand(String name, UUID xApplicationUuid) {
        try {
            return RoleGetByNameQueryDTO.builder()
                    .name(name)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar RoleGetByNameQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RoleGetAllQueryDTO toGetAllCommand(UUID xApplicationUuid) {
        try {
            return RoleGetAllQueryDTO.builder()
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar RoleGetAllQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RoleSearchQueryDTO toSearchCommand(org.springframework.data.domain.Pageable pageable, UUID xApplicationUuid) {
        try {
            return RoleSearchQueryDTO.builder()
                    .pageable(pageable)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar RoleSearchQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ========== ROLE-AUTHORITY RELATIONSHIP CONVERSIONS ==========

    public RoleAddAuthorityCommandDTO toAddAuthorityCommand(RoleAddAuthorityRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }
        try {
            return RoleAddAuthorityCommandDTO.builder()
                    .roleId(dto.getRoleId())
                    .authorityName(dto.getAuthorityName())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear RoleAddAuthorityRequestDTO para RoleAddAuthorityCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RoleAddAuthorityResponseDTO toAddAuthorityResponseDTO(RoleAddAuthorityView view) {
        if (view == null) {
            return null;
        }

        return RoleAddAuthorityResponseDTO.builder()
                .roleId(view.roleId())
                .roleName(view.roleName())
                .authorityAdded(view.authorityAdded())
                .authorities(view.authorities())
                .timestamp(view.timestamp())
                .message(view.message())
                .build();
    }

    public RoleRemoveAuthorityCommandDTO toRemoveAuthorityCommand(RoleRemoveAuthorityRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }
        try {
            return RoleRemoveAuthorityCommandDTO.builder()
                    .roleId(dto.getRoleId())
                    .authorityName(dto.getAuthorityName())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear RoleRemoveAuthorityRequestDTO para RoleRemoveAuthorityCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RoleRemoveAuthorityResponseDTO toRemoveAuthorityResponseDTO(RoleRemoveAuthorityView view) {
        if (view == null) {
            return null;
        }

        return RoleRemoveAuthorityResponseDTO.builder()
                .roleId(view.roleId())
                .roleName(view.roleName())
                .authorityRemoved(view.authorityRemoved())
                .authorities(view.authorities())
                .timestamp(view.timestamp())
                .message(view.message())
                .build();
    }
}
