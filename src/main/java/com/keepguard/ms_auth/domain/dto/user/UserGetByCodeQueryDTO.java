package com.keepguard.ms_auth.domain.dto.user;

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
public class UserGetByCodeQueryDTO {

    @NotBlank(message = "Código do usuário é obrigatório")
    private String codeUser;

    private UUID tenantId;

    @NotNull(message = "companyId é obrigatório")
    private UUID companyId;
}

