package com.keepguard.ms_auth.adapters.in.rest.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OAuthAdminAccess Tests")
class OAuthAdminAccessTest {

    private final OAuthAdminAccess access = new OAuthAdminAccess();

    @Test
    @DisplayName("permite ROLE_ADMIN")
    void shouldAllowAdmin() {
        assertDoesNotThrow(() -> access.requireAdminOrSystem(jwtWithRoles(List.of("ROLE_ADMIN"))));
    }

    @Test
    @DisplayName("permite ROLE_SYSTEM")
    void shouldAllowSystem() {
        assertDoesNotThrow(() -> access.requireAdminOrSystem(jwtWithRoles(List.of("ROLE_SYSTEM"))));
    }

    @Test
    @DisplayName("nega ROLE_USER")
    void shouldDenyUser() {
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> access.requireAdminOrSystem(jwtWithRoles(List.of("ROLE_USER"))));
        assertTrue(ex.getMessage().contains("Acesso restrito"));
    }

    @Test
    @DisplayName("nega JWT nulo")
    void shouldDenyNullJwt() {
        assertThrows(AccessDeniedException.class, () -> access.requireAdminOrSystem(null));
    }

    private Jwt jwtWithRoles(List<String> roles) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "user", "roles", roles)
        );
    }
}
