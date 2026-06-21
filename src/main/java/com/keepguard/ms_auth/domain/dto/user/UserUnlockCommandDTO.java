package com.keepguard.ms_auth.domain.dto.user;

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
public class UserUnlockCommandDTO {

    @NotBlank(message = "ID externo do usuário é obrigatório")
    private String idUserExternal;

    @NotBlank(message = "Motivo do desbloqueio é obrigatório")
    private String reason;

    @NotBlank(message = "O header X-Application é obrigatório")
    private UUID xApplicationUuid;
}

