package com.keepguard.ms_auth.application.dto.role;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoleListViewDTO(
    UUID id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

