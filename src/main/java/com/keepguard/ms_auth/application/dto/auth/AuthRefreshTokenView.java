package com.keepguard.ms_auth.application.dto.auth;

public record AuthRefreshTokenView(
    String token,
    Long expiresIn
) {}

