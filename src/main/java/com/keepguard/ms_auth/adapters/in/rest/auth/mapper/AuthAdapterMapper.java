package com.keepguard.ms_auth.adapters.in.rest.auth.mapper;

import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLoginRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLoginResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthRefreshTokenResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthRefreshTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthValidateTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthChangePasswordRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthResetPasswordRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthGenerateResetTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthGenerateResetTokenResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthRegisterLoginRequestDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthLoginCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthRefreshTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthValidateTokenQueryDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthChangePasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthResetPasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthRegisterLoginCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthLogoutCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenView;
import com.keepguard.ms_auth.application.dto.auth.AuthLogoutView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class AuthAdapterMapper {

    public AuthLoginCommandDTO toLoginCommand(AuthLoginRequestDTO dto, UUID xApplicationUuid, String userAgent) {
        if (dto == null) {
            return null;
        }

        try {
            return AuthLoginCommandDTO.builder()
                    .username(dto.getUsername() != null ? dto.getUsername().trim().toLowerCase() : null)
                    .password(dto.getPassword())
                    .xApplicationUuid(xApplicationUuid)
                    .userAgent(userAgent)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthLoginRequestDTO para AuthLoginCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthRegisterLoginCommandDTO toRegisterLoginCommand(AuthRegisterLoginRequestDTO dto, UUID xApplicationUuid, String userAgent) {
        if (dto == null) {
            return null;
        }

        try {
            return AuthRegisterLoginCommandDTO.builder()
                    .username(dto.getUsername() != null ? dto.getUsername().trim().toLowerCase() : null)
                    .passwordHash(dto.getPasswordHash())
                    .xApplicationUuid(xApplicationUuid)
                    .userAgent(userAgent)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthRegisterLoginRequestDTO para AuthRegisterLoginCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthRefreshTokenCommandDTO toRefreshTokenCommand(AuthRefreshTokenRequestDTO dto, UUID xApplicationUuid, String userAgent) {
        if (dto == null) {
            return null;
        }

        try {
            return AuthRefreshTokenCommandDTO.builder()
                    .token(dto.getToken())
                    .xApplicationUuid(xApplicationUuid)
                    .userAgent(userAgent)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthRefreshTokenRequestDTO para AuthRefreshTokenCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthValidateTokenQueryDTO toValidateTokenCommand(AuthValidateTokenRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }

        try {
            return AuthValidateTokenQueryDTO.builder()
                    .token(dto.getToken())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthValidateTokenRequestDTO para AuthValidateTokenQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthChangePasswordCommandDTO toChangePasswordCommand(AuthChangePasswordRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }

        try {
            return AuthChangePasswordCommandDTO.builder()
                    .codeUser(dto.getCodeUser())
                    .currentPassword(dto.getCurrentPassword())
                    .newPassword(dto.getNewPassword())
                    .confirmNewPassword(dto.getConfirmNewPassword())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthChangePasswordRequestDTO para AuthChangePasswordCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthResetPasswordCommandDTO toResetPasswordCommand(AuthResetPasswordRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }

        try {
            return AuthResetPasswordCommandDTO.builder()
                    .codeUser(dto.getCodeUser())
                    .resetToken(dto.getResetToken())
                    .newPassword(dto.getNewPassword())
                    .confirmNewPassword(dto.getConfirmNewPassword())
                    .messageType(dto.getMessageType())
                    .templateType(dto.getTemplateType())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthResetPasswordRequestDTO para AuthResetPasswordCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthLogoutCommandDTO toLogoutCommand(String token, UUID xApplicationUuid) {
        if (token == null) {
            return null;
        }

        try {
            return AuthLogoutCommandDTO.builder()
                    .token(token)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear token para AuthLogoutCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthLoginResponseDTO toLoginResponseDTO(AuthLoginView view) {
        if (view == null) {
            return null;
        }

        try {
            return AuthLoginResponseDTO.builder()
                    .token(view.token())
                    .expiresIn(view.expiresIn())
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthLoginView para AuthLoginResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthRefreshTokenResponseDTO toRefreshTokenResponseDTO(AuthRefreshTokenView view) {
        if (view == null) {
            return null;
        }

        try {
            return AuthRefreshTokenResponseDTO.builder()
                    .token(view.token())
                    .expiresIn(view.expiresIn())
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthRefreshTokenView para AuthRefreshTokenResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLogoutResponseDTO toLogoutResponseDTO(AuthLogoutView view) {
        if (view == null) {
            return null;
        }

        try {
            return com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLogoutResponseDTO.builder()
                    .message(view.message())
                    .success(view.success())
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthLogoutView para AuthLogoutResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthGenerateResetTokenCommandDTO toGenerateResetTokenCommand(AuthGenerateResetTokenRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }

        try {
            return AuthGenerateResetTokenCommandDTO.builder()
                    .codeUser(dto.getCodeUser())
                    .messageType(dto.getMessageType())
                    .communicationType(dto.getCommunicationType())
                    .templateType(dto.getTemplateType())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthGenerateResetTokenRequestDTO para AuthGenerateResetTokenCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthGenerateResetTokenResponseDTO toGenerateResetTokenResponseDTO(AuthGenerateResetTokenViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return AuthGenerateResetTokenResponseDTO.builder()
                    .codeUser(view.getCodeUser())
                    .messageType(view.getMessageType())
                    .communicationType(view.getCommunicationType())
                    .templateType(view.getTemplateType())
                    .token(view.getToken())
                    .expiresInSeconds(view.getExpiresInSeconds())
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AuthGenerateResetTokenViewDTO para AuthGenerateResetTokenResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
