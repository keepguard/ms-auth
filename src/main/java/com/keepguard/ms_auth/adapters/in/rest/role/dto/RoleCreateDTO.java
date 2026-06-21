package com.keepguard.ms_auth.adapters.in.rest.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de um novo role/perfil de acesso")
public class RoleCreateDTO {
    @NotBlank
    @Size(max = 50)
    @Schema(description = "Nome do role (máximo 50 caracteres)", example = "ADMIN", required = true)
    private String name;

    @Size(max = 255)
    @Schema(description = "Descrição do role (máximo 255 caracteres)", example = "Administrador do sistema", required = false)
    private String description;
}