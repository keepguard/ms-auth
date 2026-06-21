package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenView;
import com.keepguard.ms_auth.application.dto.auth.AuthLogoutView;
import com.keepguard.ms_auth.application.dto.user.UserView;
import com.keepguard.ms_auth.domain.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class AuthApplicationMapper {

    public AuthLoginView toAuthLoginView(String token, Long expiresIn) {
        if (token == null) {
            return null;
        }

        try {
            return new AuthLoginView(token, expiresIn);
        } catch (Exception e) {
            log.error("Erro ao mapear dados para AuthLoginView: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthRefreshTokenView toAuthRefreshTokenView(String token, Long expiresIn) {
        if (token == null) {
            return null;
        }

        try {
            return new AuthRefreshTokenView(token, expiresIn);
        } catch (Exception e) {
            log.error("Erro ao mapear dados para AuthRefreshTokenView: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthLogoutView toAuthLogoutView(String message, boolean success) {
        try {
            return new AuthLogoutView(message, success);
        } catch (Exception e) {
            log.error("Erro ao mapear dados para AuthLogoutView: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserView toUserView(User user) {
        if (user == null) {
            return null;
        }

        try {
            return new UserView(
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
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserView: {}", e.getMessage(), e);
            throw e;
        }
    }
}
