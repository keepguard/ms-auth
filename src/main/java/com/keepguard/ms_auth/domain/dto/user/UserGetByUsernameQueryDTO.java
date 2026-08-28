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
public class UserGetByUsernameQueryDTO {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotNull(message = "O header X-Tenant-Id é obrigatório")
    private UUID tenantId;

    @NotNull(message = "companyId é obrigatório")
    private UUID companyId;
}

