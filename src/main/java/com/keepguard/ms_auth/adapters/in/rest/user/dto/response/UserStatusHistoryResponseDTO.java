package com.keepguard.ms_auth.adapters.in.rest.user.dto.response;

import com.keepguard.ms_auth.domain.enums.UserStatusEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com dados do histórico de status do usuário")
public class UserStatusHistoryResponseDTO {

    @Schema(description = "ID do registro de histórico",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Tipo de evento que causou a mudança de status",
            example = "BLOCKED")
    private UserStatusEventType eventType;

    @Schema(description = "Motivo da mudança de status",
            example = "Violação de políticas de segurança")
    private String reason;

    @Schema(description = "Data e hora da mudança de status",
            example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}