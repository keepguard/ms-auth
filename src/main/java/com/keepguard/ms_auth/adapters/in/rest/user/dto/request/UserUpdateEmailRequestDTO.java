package com.keepguard.ms_auth.adapters.in.rest.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização de email do usuário")
public class UserUpdateEmailRequestDTO {

    @NotBlank(message = "Novo email é obrigatório")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Novo endereço de email do usuário",
            example = "novo.email@exemplo.com",
            required = true)
    private String newEmail;
}

