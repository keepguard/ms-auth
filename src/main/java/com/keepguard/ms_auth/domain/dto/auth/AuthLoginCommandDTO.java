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

    @NotBlank(message = "O header X-Tenant-Id é obrigatório")
    private UUID tenantId;

    @NotBlank(message = "companyId é obrigatório")
    private UUID companyId;

    @NotBlank(message = "O header User-Agent é obrigatório")
    private String clientId;

    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String userAgent;
}

