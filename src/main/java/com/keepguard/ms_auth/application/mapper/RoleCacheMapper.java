package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.role.RoleCacheViewDTO;
import com.keepguard.ms_auth.domain.entity.role.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleCacheMapper {

    public RoleCacheViewDTO toCacheView(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleCacheViewDTO(
            role.getId(),
            role.getName(),
            role.getDescription(),
            role.getCreatedAt(),
            role.getUpdatedAt()
        );
    }

    public Role toEntity(RoleCacheViewDTO dto) {
        if (dto == null) {
            return null;
        }

        return Role.builder()
            .id(dto.id())
            .name(dto.name())
            .description(dto.description())
            .createdAt(dto.createdAt())
            .updatedAt(dto.updatedAt())
            .build();
    }

}
