package com.keepguard.ms_auth.application.dto.auth;

public record AuthRefreshTokenViewDTO(
    String token,
    String refreshToken,
    Long expiresIn
) {}

