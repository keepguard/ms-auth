package com.keepguard.ms_auth.adapters.in.rest.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para validação de email do usuário")
public class UserValidateEmailRequestDTO {

    @NotBlank(message = "idUserExternal é obrigatório")
    @Schema(description = "ID externo do usuário cujo email será validado",
            example = "EXT123456",
            required = true)
    private String idUserExternal;
}

