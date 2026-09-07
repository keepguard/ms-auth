package com.keepguard.ms_auth.application.dto.auth;

import java.util.List;

public record AuthLoginViewDTO(
    String token,
    Long expiresIn,
    String status,
    String challengeSessionId,
    Boolean isTrusted,
    List<AvailableMfaChannelDTO> availableChannels
) {
    public AuthLoginViewDTO(String token, Long expiresIn) {
        this(token, expiresIn, "AUTHENTICATED", null, true, null);
    }
}

