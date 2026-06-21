package com.keepguard.ms_auth.adapters.in.rest.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para remover authority de um role")
public class RoleRemoveAuthorityRequestDTO {

    @NotNull(message = "ID do role é obrigatório")
    @Schema(description = "ID do role", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID roleId;

    @NotBlank(message = "Nome da authority é obrigatório")
    @Schema(description = "Nome da authority", example = "USER_READ", required = true)
    private String authorityName;
}

