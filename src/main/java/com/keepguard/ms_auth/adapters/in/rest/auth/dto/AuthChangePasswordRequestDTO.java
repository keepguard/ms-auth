package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Dados para alteração de senha do usuário")
public class AuthChangePasswordRequestDTO {

    @NotBlank
    @Schema(description = "Código único do usuário",
            example = "USR123456",
            required = true)
    private String codeUser;

    @NotBlank
    @Schema(description = "Senha atual do usuário",
            example = "SenhaAtual123!",
            required = true)
    private String currentPassword;

    @NotBlank
    @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
    @Schema(description = "Nova senha do usuário",
            example = "NovaSenha456!",
            required = true)
    private String newPassword;

    @NotBlank
    @Schema(description = "Confirmação da nova senha (deve ser igual à nova senha)",
            example = "NovaSenha456!",
            required = true)
    private String confirmNewPassword;
}
