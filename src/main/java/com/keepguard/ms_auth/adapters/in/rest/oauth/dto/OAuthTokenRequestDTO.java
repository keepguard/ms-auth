package com.keepguard.ms_auth.adapters.in.rest.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pedido de token client_credentials")
public class OAuthTokenRequestDTO {

    @NotBlank(message = "grantType é obrigatório")
    @Schema(description = "Deve ser client_credentials", example = "client_credentials", requiredMode = Schema.RequiredMode.REQUIRED)
    private String grantType;

    @NotBlank(message = "clientId é obrigatório")
    @Schema(example = "investbot-collector", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientId;

    @NotBlank(message = "clientSecret é obrigatório")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientSecret;

    @Schema(description = "UUID do agente coletor (opcional, incluído no JWT como agent_id)", example = "f7fc7350-b9fc-4e54-9c58-ac9385b23ae4")
    private String agentId;

    @Schema(description = "Código público UUID do agente coletor (opcional, incluído no JWT como agent_code)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String agentCode;
}
