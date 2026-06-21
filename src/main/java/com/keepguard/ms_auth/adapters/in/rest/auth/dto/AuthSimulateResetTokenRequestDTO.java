package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Dados para simulação de token de reset (apenas para desenvolvimento)")
public class AuthSimulateResetTokenRequestDTO {

    @Schema(description = "Código único do usuário",
            example = "USR123456",
            required = true)
    private String codeUser;

    @Schema(description = "Token de reset (opcional, se não fornecido será gerado automaticamente)",
            example = "abc123def456ghi789",
            required = false)
    private String token;

    @Schema(description = "Tempo de expiração do token em milissegundos (opcional, padrão: 15 minutos)",
            example = "900000",
            required = false)
    private Long ttlMillis; // Opcional: tempo de expiração em ms
}
