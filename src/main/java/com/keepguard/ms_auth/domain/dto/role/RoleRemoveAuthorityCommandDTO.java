package com.keepguard.ms_auth.domain.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRemoveAuthorityCommandDTO {

    @NotNull(message = "ID do role é obrigatório")
    private UUID roleId;

    @NotBlank(message = "Nome da authority é obrigatório")
    private String authorityName;

    private UUID xApplicationUuid;
}

