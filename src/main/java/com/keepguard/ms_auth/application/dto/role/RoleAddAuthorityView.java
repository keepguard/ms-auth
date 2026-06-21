package com.keepguard.ms_auth.application.dto.role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoleAddAuthorityView(
    UUID roleId,
    String roleName,
    String authorityAdded,
    List<String> authorities,
    LocalDateTime timestamp,
    String message
) {}

