package com.keepguard.ms_auth.application.dto.auth;

public record AuthRefreshTokenViewDTO(
    String token,
    Long expiresIn
) {}

