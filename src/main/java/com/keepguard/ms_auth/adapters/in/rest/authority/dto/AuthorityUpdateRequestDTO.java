package com.keepguard.ms_auth.adapters.in.rest.authority.dto;

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
@Schema(description = "Dados para atualização de uma authority/permissão")
public class AuthorityUpdateRequestDTO {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    @Schema(description = "Nome da authority (máximo 50 caracteres)", example = "USER_WRITE", required = true)
    private String name;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Schema(description = "Descrição da authority (máximo 255 caracteres)", example = "Permissão para escrita de usuários", required = false)
    private String description;
}

