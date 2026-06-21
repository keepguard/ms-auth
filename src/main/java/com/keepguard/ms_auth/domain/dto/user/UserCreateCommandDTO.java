package com.keepguard.ms_auth.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.keepguard.ms_auth.infrastructure.validation.NoForbiddenWords;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateCommandDTO {

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-z0-9._]+$", message = "Username deve conter apenas letras minúsculas, números, ponto e underline")
    @NoForbiddenWords(message = "Username contém palavras proibidas")
    private String username;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
    private String password;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotBlank(message = "ID externo é obrigatório")
    private String idUserExternal;

    private UUID codeUser;

    private UUID companyId;

    private UUID companyCode;

    @NotBlank(message = "O header X-Application é obrigatório")
    private UUID xApplicationUuid;

    private List<String> roles;
}

