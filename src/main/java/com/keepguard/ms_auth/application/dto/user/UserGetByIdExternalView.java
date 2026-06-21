package com.keepguard.ms_auth.application.dto.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * View de retorno para busca de usuário por ID externo
 * Representa os dados completos do usuário recuperado pela query findByIdUserExternal
 * Usa valores default para garantir que campos críticos não sejam null
 */
public record UserGetByIdExternalView(
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

