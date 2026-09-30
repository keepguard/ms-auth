package com.keepguard.ms_auth.application.dto.auth;

import java.util.List;

public record AuthLoginViewDTO(
    String token,
    String refreshToken,
    Long expiresIn,
    String status,
    String challengeSessionId,
    Boolean isTrusted,
    List<AvailableMfaChannelDTO> availableChannels
) {
    public AuthLoginViewDTO(String token, Long expiresIn) {
        this(token, null, expiresIn, "AUTHENTICATED", null, true, null);
    }
}

