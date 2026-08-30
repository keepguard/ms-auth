package com.keepguard.ms_auth.domain.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClientCreateCommandDTO {
    private UUID companyId;
    private String clientId;
    private String description;
    private List<String> authorities;
    private Integer tokenTtlSeconds;
}
