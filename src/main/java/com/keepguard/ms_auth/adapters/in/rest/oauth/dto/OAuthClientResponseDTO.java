package com.keepguard.ms_auth.adapters.in.rest.oauth.dto;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth client (clientSecret visível para ADMIN/SYSTEM)")
public class OAuthClientResponseDTO {
    private UUID id;
    private UUID companyId;
    private String clientId;
    private String clientSecret;
    private UUID serviceRoleId;
    private String serviceRoleName;
    private List<String> authorities;
    private OAuthClientStatus status;
    private int tokenTtlSeconds;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
