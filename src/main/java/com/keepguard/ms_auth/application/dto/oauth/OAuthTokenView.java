package com.keepguard.ms_auth.application.dto.oauth;

public record OAuthTokenView(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
