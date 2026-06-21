package com.keepguard.ms_auth.domain.dto.auth;

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
public class AuthLoginCommandDTO {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Password é obrigatório")
    private String password;

    @NotBlank(message = "O header X-Application é obrigatório")
    private UUID xApplicationUuid;

    @NotBlank(message = "O header User-Agent é obrigatório")
    private String userAgent;
}

