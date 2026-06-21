package com.keepguard.ms_auth.adapters.in.rest.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta da busca de role por nome")
public class RoleGetByNameResponseDTO {

    @Schema(description = "ID único da role", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nome da role", example = "ADMIN")
    private String name;

    @Schema(description = "Descrição da role", example = "Role de administrador do sistema")
    private String description;

    @Schema(description = "Data de criação da role")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização da role")
    private LocalDateTime updatedAt;

    @Schema(description = "Indica se a role está ativa", example = "true")
    private Boolean active;
}
