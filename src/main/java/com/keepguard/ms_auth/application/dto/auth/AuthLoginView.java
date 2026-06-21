package com.keepguard.ms_auth.application.dto.auth;

public record AuthLoginView(
    String token,
    Long expiresIn
) {}

