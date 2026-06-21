package com.keepguard.ms_auth.application.dto.auth;

public record AuthLogoutView(
    String message,
    boolean success
) {}

