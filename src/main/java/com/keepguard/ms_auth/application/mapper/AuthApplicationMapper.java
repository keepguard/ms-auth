package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.auth.AuthLoginViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLogoutViewDTO;
import com.keepguard.ms_auth.application.dto.user.UserViewDTO;
import com.keepguard.ms_auth.domain.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class AuthApplicationMapper {

    public AuthLoginViewDTO toAuthLoginView(String token, Long expiresIn) {
        if (token == null) {
            return null;
        }

        try {
            return new AuthLoginViewDTO(token, expiresIn);
        } catch (Exception e) {
            log.error("Erro ao mapear dados para AuthLoginViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthRefreshTokenViewDTO toAuthRefreshTokenView(String token, Long expiresIn) {
        if (token == null) {
            return null;
        }

        try {
            return new AuthRefreshTokenViewDTO(token, expiresIn);
        } catch (Exception e) {
            log.error("Erro ao mapear dados para AuthRefreshTokenViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthLogoutViewDTO toAuthLogoutView(String message, boolean success) {
        try {
            return new AuthLogoutViewDTO(message, success);
        } catch (Exception e) {
            log.error("Erro ao mapear dados para AuthLogoutViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserViewDTO toUserView(User user) {
        if (user == null) {
            return null;
        }

        try {
            return new UserViewDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // name - não disponível na entidade User atual
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : null,
                user.getCodeUser() != null ? user.getCodeUser().toString() : null,
                user.getStatus() != null ? user.getStatus().toString() : null,
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                null, // roles - será preenchido pelo UserQueryService
                user.getCompanyId(),
                user.getCompanyCode()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
