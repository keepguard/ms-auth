package com.keepguard.ms_auth.adapters.in.rest.authority.mapper;

import com.keepguard.ms_auth.adapters.in.rest.authority.dto.*;
import com.keepguard.ms_auth.domain.dto.authority.*;
import com.keepguard.ms_auth.application.dto.authority.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class AuthorityAdapterMapper {

    // ========== COMMAND DTO CONVERSIONS ==========

    public AuthorityCreateCommandDTO toCreateCommand(AuthorityCreateRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        try {
            return AuthorityCreateCommandDTO.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthorityCreateRequestDTO para AuthorityCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthorityUpdateCommandDTO toUpdateCommand(UUID id, AuthorityUpdateRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        try {
            return AuthorityUpdateCommandDTO.builder()
                    .id(id)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthorityUpdateRequestDTO para AuthorityUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthorityDeleteCommandDTO toDeleteCommand(UUID id) {
        try {
            return AuthorityDeleteCommandDTO.builder()
                    .id(id)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar AuthorityDeleteCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthorityGetByIdQueryDTO toGetByIdQuery(UUID id) {
        try {
            return AuthorityGetByIdQueryDTO.builder()
                    .id(id)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar AuthorityGetByIdQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthorityGetByNameQueryDTO toGetByNameQuery(String name) {
        try {
            return AuthorityGetByNameQueryDTO.builder()
                    .name(name)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar AuthorityGetByNameQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthorityGetAllQueryDTO toGetAllQuery() {
        try {
            return AuthorityGetAllQueryDTO.builder()
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar AuthorityGetAllQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthoritySearchQueryDTO toSearchQuery(AuthoritySearchRequestDTO searchRequest) {
        try {
            Sort.Direction direction = "ASC".equalsIgnoreCase(searchRequest.getSortDirection())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            
            String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "createdAt";
            
            PageRequest pageable = PageRequest.of(
                    searchRequest.getPage(),
                    searchRequest.getSize(),
                    Sort.by(direction, sortBy)
            );
            
            return AuthoritySearchQueryDTO.builder()
                    .name(searchRequest.getName())
                    .description(searchRequest.getDescription())
                    .pageable(pageable)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao criar AuthoritySearchQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ========== VIEW TO RESPONSE DTO CONVERSIONS ==========

    public AuthorityCreateResponseDTO toCreateResponseDTO(AuthorityCreateView view) {
        if (view == null) {
            return null;
        }

        return AuthorityCreateResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public AuthorityUpdateResponseDTO toUpdateResponseDTO(AuthorityUpdateView view) {
        if (view == null) {
            return null;
        }

        return AuthorityUpdateResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public AuthorityGetByIdResponseDTO toGetByIdResponseDTO(AuthorityGetByIdView view) {
        if (view == null) {
            return null;
        }

        return AuthorityGetByIdResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public AuthorityGetByNameResponseDTO toGetByNameResponseDTO(AuthorityGetByNameView view) {
        if (view == null) {
            return null;
        }

        return AuthorityGetByNameResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public AuthorityListResponseDTO toListResponseDTO(AuthorityListView view) {
        if (view == null) {
            return null;
        }

        return AuthorityListResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public AuthoritySearchResponseDTO toSearchResponseDTO(AuthoritySearchView view) {
        if (view == null) {
            return null;
        }

        return AuthoritySearchResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }
}

