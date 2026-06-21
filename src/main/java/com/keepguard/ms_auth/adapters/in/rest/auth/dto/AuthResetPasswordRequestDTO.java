package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import com.keepguard.lib_common.communication.enums.MessageTypeEnum;
import com.keepguard.lib_common.communication.enums.TemplateTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Dados para redefinição de senha usando token de reset")
public class AuthResetPasswordRequestDTO {

    @NotBlank
    @Schema(description = "Código único do usuário",
            example = "USR123456",
            required = true)
    private String codeUser;

    @NotBlank
    @Schema(description = "Token de reset enviado por email",
            example = "abc123def456ghi789",
            required = true)
    private String resetToken;

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

    @NotNull
    @Schema(description = "Tipo da mensagem utilizada para gerar o token",
            example = "EMAIL",
            required = true)
    private MessageTypeEnum messageType;

    @NotNull
    @Schema(description = "Tipo do template utilizado para gerar o token",
            example = "RECUPERACAO_SENHA",
            required = true)
    private TemplateTypeEnum templateType;
}
