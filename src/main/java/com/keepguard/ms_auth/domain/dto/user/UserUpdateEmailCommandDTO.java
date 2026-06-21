package com.keepguard.ms_auth.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateEmailCommandDTO {

    @NotBlank(message = "ID externo do usuário é obrigatório")
    private String idUserExternal;

    @NotBlank(message = "Novo email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String newEmail;

    @NotBlank(message = "O header X-Application é obrigatório")
    private UUID xApplicationUuid;
}

