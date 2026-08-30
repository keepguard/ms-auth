package com.keepguard.ms_auth.application.dto.oauth;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OAuthClientCreateView(
        UUID id,
        UUID companyId,
        String clientId,
        String clientSecret,
        List<String> authorities,
        OAuthClientStatus status,
        int tokenTtlSeconds,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
