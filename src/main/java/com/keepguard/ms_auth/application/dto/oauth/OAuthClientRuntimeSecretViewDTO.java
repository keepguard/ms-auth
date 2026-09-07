package com.keepguard.ms_auth.application.dto.oauth;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;

public record OAuthClientRuntimeSecretViewDTO(
        String clientId,
        String secretEncrypted,
        OAuthClientStatus status
) {
}
