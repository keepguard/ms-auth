package com.keepguard.ms_auth.adapters.in.rest.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com dados básicos do usuário")
public class UserResponseDTO {

    @Schema(description = "ID interno do usuário no sistema",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome de usuário",
            example = "joao.silva")
    private String username;

    @Schema(description = "Endereço de email do usuário",
            example = "joao.silva@exemplo.com")
    private String email;

    @Schema(description = "Status atual do usuário",
            example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Indica se o email foi verificado",
            example = "true")
    private boolean emailVerified;

    @Schema(description = "Data e hora de criação do usuário",
            example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da última atualização do usuário",
            example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "ID da empresa à qual o usuário pertence",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID companyId;

    @Schema(description = "Código da empresa à qual o usuário pertence",
            example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID companyCode;

    @Schema(description = "Identificador único da aplicação",
            example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID xApplication;
}