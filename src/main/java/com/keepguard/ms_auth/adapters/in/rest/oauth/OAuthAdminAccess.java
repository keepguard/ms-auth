package com.keepguard.ms_auth.adapters.in.rest.oauth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class OAuthAdminAccess {

    public void requireAdminOrSystem(Jwt jwt) {
        if (jwt == null) {
            throw new AccessDeniedException("Token JWT não informado ou inválido.");
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null || roles.isEmpty()) {
            String single = jwt.getClaimAsString("roles");
            if (single != null && !single.isBlank()) {
                roles = List.of(single);
            }
        }
        if (roles == null || roles.stream().noneMatch(OAuthAdminAccess::isAdminOrSystem)) {
            throw new AccessDeniedException("Acesso restrito a administradores e SYSTEM.");
        }
    }

    static boolean isAdminOrSystem(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return "ADMIN".equals(normalized) || "SYSTEM".equals(normalized);
    }
}
