package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.domain.entity.role.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleApplicationMapper {

    public RoleCreateViewDTO toCreateView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleCreateViewDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleUpdateViewDTO toUpdateView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleUpdateViewDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleGetByIdViewDTO toGetByIdView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleGetByIdViewDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleGetByNameViewDTO toGetByNameView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleGetByNameViewDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleListViewDTO toListView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleListViewDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleSearchViewDTO toSearchView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleSearchViewDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleAddAuthorityViewDTO toAddAuthorityView(Role role, String authorityAdded) {
        if (role == null) {
            return null;
        }

        var authorityNames = role.getAuthorities().stream()
                .map(auth -> auth.getName())
                .sorted()
                .toList();

        return new RoleAddAuthorityViewDTO(
                role.getId(),
                role.getName(),
                authorityAdded,
                authorityNames,
                java.time.LocalDateTime.now(),
                "Authority adicionada com sucesso ao role"
        );
    }

    public RoleRemoveAuthorityViewDTO toRemoveAuthorityView(Role role, String authorityRemoved) {
        if (role == null) {
            return null;
        }

        var authorityNames = role.getAuthorities().stream()
                .map(auth -> auth.getName())
                .sorted()
                .toList();

        return new RoleRemoveAuthorityViewDTO(
                role.getId(),
                role.getName(),
                authorityRemoved,
                authorityNames,
                java.time.LocalDateTime.now(),
                "Authority removida com sucesso do role"
        );
    }
}
