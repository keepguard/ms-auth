package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.domain.entity.role.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleApplicationMapper {

    public RoleCreateView toCreateView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleCreateView(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleUpdateView toUpdateView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleUpdateView(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleGetByIdView toGetByIdView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleGetByIdView(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleGetByNameView toGetByNameView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleGetByNameView(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleListView toListView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleListView(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleSearchView toSearchView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleSearchView(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public RoleAddAuthorityView toAddAuthorityView(Role role, String authorityAdded) {
        if (role == null) {
            return null;
        }

        var authorityNames = role.getAuthorities().stream()
                .map(auth -> auth.getName())
                .sorted()
                .toList();

        return new RoleAddAuthorityView(
                role.getId(),
                role.getName(),
                authorityAdded,
                authorityNames,
                java.time.LocalDateTime.now(),
                "Authority adicionada com sucesso ao role"
        );
    }

    public RoleRemoveAuthorityView toRemoveAuthorityView(Role role, String authorityRemoved) {
        if (role == null) {
            return null;
        }

        var authorityNames = role.getAuthorities().stream()
                .map(auth -> auth.getName())
                .sorted()
                .toList();

        return new RoleRemoveAuthorityView(
                role.getId(),
                role.getName(),
                authorityRemoved,
                authorityNames,
                java.time.LocalDateTime.now(),
                "Authority removida com sucesso do role"
        );
    }
}
