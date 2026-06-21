package com.keepguard.ms_auth.application.dto.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * View de retorno para busca de usuário por código
 * Representa os dados completos do usuário recuperado pela query findByCodeUser
 */
public record UserGetByCodeView(
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

