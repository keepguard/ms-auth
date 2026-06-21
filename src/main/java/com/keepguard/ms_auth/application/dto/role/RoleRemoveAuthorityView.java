package com.keepguard.ms_auth.application.dto.role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoleRemoveAuthorityView(
    UUID roleId,
    String roleName,
    String authorityRemoved,
    List<String> authorities,
    LocalDateTime timestamp,
    String message
) {}

