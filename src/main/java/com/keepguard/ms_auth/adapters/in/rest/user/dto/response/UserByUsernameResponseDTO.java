package com.keepguard.ms_auth.adapters.in.rest.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta da busca de usuário por username")
public class UserByUsernameResponseDTO {

    @Schema(description = "ID interno do usuário no sistema",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID externo do usuário",
            example = "EXT123456")
    private UUID idUserExternal;

    @Schema(description = "Código único do usuário",
            example = "USR123456")
    private UUID codeUser;

    @Schema(description = "Nome de usuário",
            example = "joao.silva")
    private String username;

    @Schema(description = "Endereço de email do usuário",
            example = "joao.silva@exemplo.com")
    private String email;

    @Schema(description = "Status atual do usuário",
            example = "ACTIVE")
    private String status;

    @Schema(description = "Indica se o email foi verificado",
            example = "true")
    private Boolean emailVerified;

    @Schema(description = "Data e hora de criação do usuário (formato: dd/MM/yyyy HH:mm:ss)",
            example = "15/01/2024 10:30:00")
    private String createdAt;

    @Schema(description = "Data e hora da última atualização do usuário (formato: dd/MM/yyyy HH:mm:ss)",
            example = "15/01/2024 10:30:00")
    private String updatedAt;

    @Schema(description = "Data e hora do último login do usuário (formato: dd/MM/yyyy HH:mm:ss)",
            example = "15/01/2024 10:30:00")
    private String lastLogin;

    @Schema(description = "Lista de roles/perfis do usuário",
            example = "[\"USER\", \"ADMIN\"]")
    private List<String> roles;

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

