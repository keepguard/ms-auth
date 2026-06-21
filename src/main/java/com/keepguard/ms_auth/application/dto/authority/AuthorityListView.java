package com.keepguard.ms_auth.application.dto.authority;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthorityListView(
    UUID id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

