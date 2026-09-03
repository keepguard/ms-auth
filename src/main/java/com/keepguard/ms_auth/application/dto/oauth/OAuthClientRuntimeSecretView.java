package com.keepguard.ms_auth.application.dto.oauth;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;

public record OAuthClientRuntimeSecretView(
        String clientId,
        String secretEncrypted,
        OAuthClientStatus status
) {
}
