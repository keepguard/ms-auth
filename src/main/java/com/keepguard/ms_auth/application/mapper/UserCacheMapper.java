package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.user.UserAuthCacheViewDTO;
import com.keepguard.ms_auth.application.dto.user.UserRolesCacheViewDTO;
import com.keepguard.ms_auth.domain.entity.user.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserCacheMapper {

    public UserAuthCacheViewDTO toAuthCacheView(User user) {
        if (user == null) {
            return null;
        }

        return new UserAuthCacheViewDTO(
            user.getId(),
            user.getIdUserExternal(),
            user.getCodeUser(),
            user.getUsername(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getStatus(),
            user.getEmailVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLogin(),
            user.getCompanyId(),
            user.getCompanyCode(),
            user.getTenantId()
        );
    }

    public User toEntity(UserAuthCacheViewDTO dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
            .id(dto.id())
            .idUserExternal(dto.idUserExternal())
            .codeUser(dto.codeUser())
            .username(dto.username())
            .email(dto.email())
            .passwordHash(dto.passwordHash())
            .status(dto.status())
            .emailVerified(dto.emailVerified())
            .createdAt(dto.createdAt())
            .updatedAt(dto.updatedAt())
            .lastLogin(dto.lastLogin())
            .companyId(dto.companyId())
            .companyCode(dto.companyCode())
            .tenantId(dto.tenantId())
            .build();
    }

    public UserRolesCacheViewDTO toUserRolesCacheView(java.util.UUID codeUser, List<String> roles) {
        if (codeUser == null || roles == null) {
            return null;
        }

        return new UserRolesCacheViewDTO(codeUser, roles);
    }

}
