package com.keepguard.ms_auth.adapters.in.rest.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO contendo o motivo para alteração de status do usuário")
public class UserStatusReasonRequestDTO {

    @Schema(description = "Motivo da alteração de status (bloqueio, desbloqueio, exclusão, etc.)",
            example = "Violação de políticas de segurança",
            required = true)
    private String reason;
}

