package com.keepguard.ms_auth.application.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record UserSearchCriteriaView(
    String id,
    String username,
    String email,
    String name,
    String idUserExternal,
    String codeUser,
    String status,
    String role,
    List<String> roles,
    String companyId,
    String companyCode,
    Boolean emailVerified,
    LocalDateTime createdAtFrom,
    LocalDateTime createdAtTo,
    LocalDateTime updatedAtFrom,
    LocalDateTime updatedAtTo,
    LocalDateTime lastLoginFrom,
    LocalDateTime lastLoginTo,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {}
