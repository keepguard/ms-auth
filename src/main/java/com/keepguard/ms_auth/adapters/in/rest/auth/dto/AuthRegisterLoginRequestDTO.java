package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para login após registro com senha criptografada")
public class AuthRegisterLoginRequestDTO {

    @Schema(description = "Username do usuário (email)", example = "rafael@example.com")
    @NotBlank(message = "username é obrigatório")
    @Size(max = 255, message = "username deve ter no máximo 255 caracteres")
    private String username;

    @Schema(description = "Hash da senha criptografada", example = "$2a$10$hashed...")
    @NotBlank(message = "passwordHash é obrigatório")
    @Size(max = 255, message = "passwordHash deve ter no máximo 255 caracteres")
    private String passwordHash;

    @Schema(description = "UUID da aplicação", example = "123e4567-e89b-12d3-a456-426614174000")
    private String xApplication;
}
