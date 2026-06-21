package com.keepguard.ms_auth.domain.dto.auth;

import java.util.UUID;

import com.keepguard.lib_common.communication.enums.MessageTypeEnum;
import com.keepguard.lib_common.communication.enums.TemplateTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResetPasswordCommandDTO {

    @NotBlank(message = "Código do usuário é obrigatório")
    private String codeUser;

    @NotBlank(message = "Token de reset é obrigatório")
    private String resetToken;

    @NotBlank(message = "Nova senha é obrigatória")
    private String newPassword;

    @NotBlank(message = "Confirmação da nova senha é obrigatória")
    private String confirmNewPassword;

    @NotBlank(message = "O header X-Application é obrigatório")
    private UUID xApplicationUuid;

    @NotNull(message = "Message Type é obrigatório")
    private MessageTypeEnum messageType;

    @NotNull(message = "Template Type é obrigatório")
    private TemplateTypeEnum templateType;
}

