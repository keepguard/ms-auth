package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para validação de token JWT")
public class AuthValidateTokenRequestDTO {

    @NotBlank(message = "Token é obrigatório")
    @Schema(description = "Token JWT a ser validado",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true)
    private String token;
}
