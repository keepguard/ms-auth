package com.keepguard.ms_auth.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de roles do usuário para cache")
public record UserRolesCacheView(
    @Schema(description = "Código único do usuário")
    UUID codeUser,

    @Schema(description = "Lista de roles do usuário")
    List<String> roles
) {}
