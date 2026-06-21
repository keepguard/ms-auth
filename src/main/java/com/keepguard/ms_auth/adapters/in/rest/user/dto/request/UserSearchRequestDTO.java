package com.keepguard.ms_auth.adapters.in.rest.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parâmetros de busca para usuários")
public class UserSearchRequestDTO {

    @Schema(description = "ID interno do usuário",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID externo do usuário",
            example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID idUserExternal;

    @Schema(description = "Código único do usuário",
            example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID codeUser;

    @Schema(description = "ID da empresa",
            example = "550e8400-e29b-41d4-a716-446655440003")
    private UUID companyId;

    @Schema(description = "Código da empresa",
            example = "550e8400-e29b-41d4-a716-446655440004")
    private UUID companyCode;

    @Schema(description = "Nome de usuário (busca parcial)",
            example = "joao")
    private String username;

    @Schema(description = "Email do usuário (busca parcial)",
            example = "joao@exemplo")
    private String email;

    @Schema(description = "Status do usuário",
            example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Indica se o email foi verificado",
            example = "true")
    private Boolean emailVerified;

    @Schema(description = "Data inicial de criação (formato: dd/MM/yyyy HH:mm:ss)",
            example = "01/01/2024 00:00:00")
    private String createdAtStart;

    @Schema(description = "Data final de criação (formato: dd/MM/yyyy HH:mm:ss)",
            example = "31/12/2024 23:59:59")
    private String createdAtEnd;

    @Schema(description = "Data inicial de atualização (formato: dd/MM/yyyy HH:mm:ss)",
            example = "01/01/2024 00:00:00")
    private String updatedAtStart;

    @Schema(description = "Data final de atualização (formato: dd/MM/yyyy HH:mm:ss)",
            example = "31/12/2024 23:59:59")
    private String updatedAtEnd;

    @Schema(description = "Data inicial do último login (formato: dd/MM/yyyy HH:mm:ss)",
            example = "01/01/2024 00:00:00")
    private String lastLoginStart;

    @Schema(description = "Data final do último login (formato: dd/MM/yyyy HH:mm:ss)",
            example = "31/12/2024 23:59:59")
    private String lastLoginEnd;

    @Schema(description = "Lista de roles para filtrar usuários que possuem",
            example = "[\"ADMIN\", \"USER\"]")
    private List<String> roles;

    @Schema(description = "Número da página (opcional, padrão: 0)",
            example = "0")
    private Integer page;

    @Schema(description = "Tamanho da página (opcional, padrão: 10)",
            example = "10")
    private Integer size;

    @Schema(description = "Campo para ordenação dos resultados. " +
            "Campos válidos: id, username, email, createdAt, updatedAt, lastLogin. " +
            "Padrão: createdAt",
            example = "createdAt",
            allowableValues = {"id", "username", "email", "createdAt", "updatedAt", "lastLogin"})
    private String sortBy;

    @Schema(description = "Direção da ordenação. " +
            "Valores válidos: ASC (crescente), DESC (decrescente). " +
            "Padrão: DESC",
            example = "DESC",
            allowableValues = {"ASC", "DESC"})
    private String sortDirection;
}