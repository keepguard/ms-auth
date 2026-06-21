package com.keepguard.ms_auth.infrastructure.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoForbiddenWordsValidatorTest {

    @Test
    @DisplayName("Deve aprovar username válido sem palavras proibidas e reprovar quando contém proibidas")
    void shouldApproveValidUsername() {
        NoForbiddenWordsValidator v = new NoForbiddenWordsValidator();
        v.initialize(null);

        assertTrue(v.isValid("joao.silva", null));
        assertFalse(v.isValid("user_123.ok", null)); // 'user' é proibida
    }

    @Test
    @DisplayName("Deve reprovar quando contém palavra proibida (admin/test)")
    void shouldRejectWhenContainsForbiddenWord() {
        NoForbiddenWordsValidator v = new NoForbiddenWordsValidator();
        v.initialize(null);

        assertFalse(v.isValid("admin123", null));
        assertFalse(v.isValid("xxTESTxx", null));
        assertFalse(v.isValid("the_user_is_fake", null));
    }

    @Test
    @DisplayName("Null e vazio são tratados por outras validações e retornam true aqui")
    void nullAndBlankReturnTrue() {
        NoForbiddenWordsValidator v = new NoForbiddenWordsValidator();
        v.initialize(null);

        assertTrue(v.isValid(null, null));
        assertTrue(v.isValid("   ", null));
    }
}


