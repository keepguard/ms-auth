package com.keepguard.ms_auth.application.dto.oauth;

public record OAuthTokenViewDTO(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
