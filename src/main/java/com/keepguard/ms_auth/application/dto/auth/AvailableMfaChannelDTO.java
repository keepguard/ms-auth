package com.keepguard.ms_auth.application.dto.auth;

public record AvailableMfaChannelDTO(
    String channel,
    String targetMasked,
    String description
) {}
