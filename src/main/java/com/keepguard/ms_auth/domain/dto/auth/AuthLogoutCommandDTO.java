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
public class AuthLogoutCommandDTO {

    @NotBlank(message = "Token é obrigatório")
    private String token;

    @NotBlank(message = "O header X-Application é obrigatório")
    private UUID xApplicationUuid;
}

