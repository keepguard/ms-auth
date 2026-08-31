package com.keepguard.ms_auth.application.dto.oauth;

import java.util.List;
import java.util.UUID;

public record OAuthServiceRoleView(
        UUID id,
        String name,
        String description,
        List<OAuthServiceRoleAuthorityView> authorities
) {
}
