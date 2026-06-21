package com.keepguard.ms_auth.application.dto.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserView(
    UUID id,
    String username,
    String email,
    String name,
    String idUserExternal,
    String codeUser,
    String status,
    Boolean emailVerified,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastLogin,
    List<String> roles,
    UUID companyId,
    UUID companyCode,
    UUID xApplication
) {}
