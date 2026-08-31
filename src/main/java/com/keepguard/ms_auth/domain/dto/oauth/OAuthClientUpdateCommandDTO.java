package com.keepguard.ms_auth.domain.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClientUpdateCommandDTO {
    private UUID companyId;
    private UUID id;
    private String description;
    private UUID roleId;
    private Integer tokenTtlSeconds;
}
