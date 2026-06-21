package com.keepguard.ms_auth.adapters.in.rest.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta da remoção de authority do role")
public class RoleRemoveAuthorityResponseDTO {

    @Schema(description = "ID do role", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID roleId;

    @Schema(description = "Nome do role", example = "ADMIN")
    private String roleName;

    @Schema(description = "Nome da authority removida", example = "USER_READ")
    private String authorityRemoved;

    @Schema(description = "Lista de todas as authorities do role após a remoção")
    private List<String> authorities;

    @Schema(description = "Data/hora da operação")
    private LocalDateTime timestamp;

    @Schema(description = "Mensagem de sucesso")
    private String message;
}

