package com.keepguard.ms_auth.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserHardDeleteCommandDTO(
    @NotBlank(message = "idUserExternal é obrigatório")
    String idUserExternal,
    
    @NotNull(message = "companyId é obrigatório")
    UUID companyId
) {
}
