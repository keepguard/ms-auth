package com.keepguard.ms_auth.adapters.in.rest.authority.dto;

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
@Schema(description = "Resposta da criação de authority")
public class AuthorityCreateResponseDTO {

    @Schema(description = "ID único da authority", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nome da authority", example = "USER_READ")
    private String name;

    @Schema(description = "Descrição da authority", example = "Permissão para leitura de usuários")
    private String description;

    @Schema(description = "Data de criação da authority")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização da authority")
    private LocalDateTime updatedAt;
}

