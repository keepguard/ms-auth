package com.keepguard.ms_auth.application.dto.user;

import com.keepguard.ms_auth.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de usuário para cache")
public record UserAuthCacheView(
    @Schema(description = "ID único do usuário")
    UUID id,

    @Schema(description = "ID do usuário externo")
    UUID idUserExternal,

    @Schema(description = "Código único do usuário")
    UUID codeUser,

    @Schema(description = "Nome de usuário (login)")
    String username,

    @Schema(description = "Email do usuário")
    String email,

    @Schema(description = "Hash da senha")
    String passwordHash,

    @Schema(description = "Status do usuário")
    UserStatus status,

    @Schema(description = "Email verificado")
    Boolean emailVerified,

    @Schema(description = "Data de criação")
    LocalDateTime createdAt,

    @Schema(description = "Data de atualização")
    LocalDateTime updatedAt,

    @Schema(description = "Último login")
    LocalDateTime lastLogin,

    @Schema(description = "ID da empresa")
    UUID companyId,

    @Schema(description = "Código da empresa")
    UUID companyCode,

    @Schema(description = "ID da aplicação")
    UUID xApplication
) {}
