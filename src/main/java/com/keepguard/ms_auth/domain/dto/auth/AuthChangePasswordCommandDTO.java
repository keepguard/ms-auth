package com.keepguard.ms_auth.domain.dto.auth;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthChangePasswordCommandDTO {

    @NotBlank(message = "Código do usuário é obrigatório")
    private String codeUser;

    @NotBlank(message = "Senha atual é obrigatória")
    private String currentPassword;

    @NotBlank(message = "Nova senha é obrigatória")
    private String newPassword;

    @NotBlank(message = "Confirmação da nova senha é obrigatória")
    private String confirmNewPassword;

    @NotBlank(message = "O header X-Application é obrigatório")
    private UUID xApplicationUuid;
    
}

