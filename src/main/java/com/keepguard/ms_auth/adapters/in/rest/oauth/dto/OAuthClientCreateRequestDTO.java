package com.keepguard.ms_auth.adapters.in.rest.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de um OAuth client de sistema")
public class OAuthClientCreateRequestDTO {

    @NotBlank(message = "clientId é obrigatório")
    @Size(max = 100, message = "clientId deve ter no máximo 100 caracteres")
    @Schema(description = "Identificador público do client", example = "investbot-collector", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientId;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Schema(description = "Descrição do client")
    private String description;

    @NotNull(message = "roleId é obrigatório")
    @Schema(description = "ID da service role global", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID roleId;

    @Schema(description = "TTL do JWT em segundos (900 a 86400). Padrão 28800 (8h).", example = "28800")
    private Integer tokenTtlSeconds;
}
