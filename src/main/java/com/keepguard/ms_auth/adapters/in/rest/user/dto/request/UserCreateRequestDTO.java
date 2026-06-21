package com.keepguard.ms_auth.adapters.in.rest.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.keepguard.ms_auth.infrastructure.validation.NoForbiddenWords;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de um novo usuário no sistema")
public class UserCreateRequestDTO {

    @Schema(description = "Username é o campo usado para login do usuário",
            example = "joao.silva@exemplo.com")
    private String username;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Endereço de email do usuário",
            example = "joao.silva@exemplo.com",
            required = true)
    private String email;

    @NotBlank(message = "Password é obrigatório")
    @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
    @Schema(description = "Senha do usuário (deve atender aos critérios de segurança)",
            example = "MinhaSenha123!",
            required = true)
    private String password;

    @JsonProperty("id_user_external")
    @NotBlank(message = "idUserExternal é obrigatório")
    @Schema(description = "ID externo do usuário (identificador único no sistema externo)",
            example = "EXT123456",
            required = true)
    private String idUserExternal;

    @JsonProperty("code_user")
    @NotBlank(message = "codeUser é obrigatório")
    @Schema(description = "Código único do usuário no sistema",
            example = "USR123456",
            required = true)
    private String codeUser;

    @JsonProperty("company_id")
    @NotBlank(message = "ID da empresa é obrigatório")
    @Schema(description = "ID da empresa à qual o usuário pertence",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true)
    private String companyId;

    @JsonProperty("company_code")
    @NotBlank(message = "Código da empresa é obrigatório")
    @Schema(description = "Código da empresa à qual o usuário pertence",
            example = "550e8400-e29b-41d4-a716-446655440001",
            required = true)
    private String companyCode;

    @JsonProperty("x_application")
    @NotBlank(message = "xApplication é obrigatório")
    @Schema(description = "Identificador único da aplicação",
            example = "550e8400-e29b-41d4-a716-446655440002",
            required = true)
    private String xApplication;
}

