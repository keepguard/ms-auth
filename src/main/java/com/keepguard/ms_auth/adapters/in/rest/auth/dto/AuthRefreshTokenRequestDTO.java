package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRefreshTokenRequestDTO {

    @NotBlank(message = "Token é obrigatório")
    private String token;

    /**
     * Refresh token opaco (novo fluxo). Quando presente, tem prioridade sobre
     * {@code token} para a rotação — {@code token} continua aceito por
     * compatibilidade com clientes que ainda mandam o JWT como refresh.
     */
    private String refreshToken;
}
