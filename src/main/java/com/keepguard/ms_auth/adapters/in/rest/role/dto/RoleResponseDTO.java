package com.keepguard.ms_auth.adapters.in.rest.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com dados do role/perfil de acesso")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponseDTO {
    @Schema(description = "ID do role", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome do role", example = "ADMIN")
    private String name;

    @Schema(description = "Descrição do role", example = "Administrador do sistema")
    private String description;

    @Schema(description = "Data de criação do role", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização do role", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}