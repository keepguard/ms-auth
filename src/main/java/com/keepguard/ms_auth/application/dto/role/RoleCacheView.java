package com.keepguard.ms_auth.application.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de role para cache")
public record RoleCacheView(
    @Schema(description = "ID único da role")
    UUID id,

    @Schema(description = "Nome da role")
    String name,

    @Schema(description = "Descrição da role")
    String description,

    @Schema(description = "Data de criação")
    LocalDateTime createdAt,

    @Schema(description = "Data de atualização")
    LocalDateTime updatedAt
) {}
