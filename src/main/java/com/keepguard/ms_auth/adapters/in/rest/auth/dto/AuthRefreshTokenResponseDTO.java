package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de refresh token contendo o novo token JWT")
public class AuthRefreshTokenResponseDTO {

    @Schema(description = "Novo token JWT para autenticação nas requisições subsequentes",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true)
    private String token;

    @Schema(description = "Tempo de expiração do novo token em segundos",
            example = "3600",
            required = false)
    private Long expiresIn;
}