package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import com.keepguard.ms_auth.application.dto.auth.AvailableMfaChannelDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de login contendo o token JWT ou desafio de MFA de dispositivo")
public class AuthLoginResponseDTO {

    @Schema(description = "Token JWT para autenticação nas requisições subsequentes",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = false)
    private String token;

    @Schema(description = "Tempo de expiração do token em segundos",
            example = "3600",
            required = false)
    private Long expiresIn;

    @Schema(description = "Status da autenticação (AUTHENTICATED ou MFA_REQUIRED)",
            example = "MFA_REQUIRED",
            required = false)
    private String status;

    @Schema(description = "Identificador da sessão de desafio de dispositivo",
            example = "chal_98a72f1e-45bc-44a1-89fa-123456789abc",
            required = false)
    private String challengeSessionId;

    @Schema(description = "Indica se o dispositivo já é confiável",
            example = "false",
            required = false)
    private Boolean isTrusted;

    @Schema(description = "Lista de canais de MFA disponíveis para o usuário escolher",
            required = false)
    private List<AvailableMfaChannelDTO> availableChannels;
}