package com.keepguard.ms_auth.domain.entity.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserCreateNewInvariantTest {

    @Test
    @DisplayName("createNew deve normalizar para lowercase e aceitar regex válida")
    void createNew_shouldNormalizeAndAccept() {
        UUID app = UUID.randomUUID();
        UUID idExt = UUID.randomUUID();
        User u = User.createNew("  Abc.Def_123  ", "test@example.com", "hash", idExt, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), app);
        assertEquals("abc.def_123", u.getUsername());
    }

    @Test
    @DisplayName("createNew deve lançar IllegalArgumentException para username inválido")
    void createNew_shouldRejectInvalid() {
        UUID app = UUID.randomUUID();
        UUID idExt = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
            User.createNew("__", "test@example.com", "hash", idExt, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), app)
        );
        assertThrows(IllegalArgumentException.class, () ->
            User.createNew("INVALID-UPPER-AND-CHAR$", "test@example.com", "hash", idExt, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), app)
        );
    }
}


