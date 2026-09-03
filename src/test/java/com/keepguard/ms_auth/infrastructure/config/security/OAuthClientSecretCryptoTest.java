package com.keepguard.ms_auth.infrastructure.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OAuthClientSecretCrypto")
class OAuthClientSecretCryptoTest {

    @Test
    @DisplayName("composeForHash concatena plaintext + BASE")
    void composeForHash_appendsBase() {
        OAuthClientSecretCrypto crypto = new OAuthClientSecretCrypto("base-key");
        assertEquals("plainbase-key", crypto.composeForHash("plain"));
    }

    @Test
    @DisplayName("encrypt/decrypt round-trip")
    void encryptDecrypt_roundTrip() {
        OAuthClientSecretCrypto crypto = new OAuthClientSecretCrypto("base-key");
        String encrypted = crypto.encrypt("client-secret-value");
        assertNotEquals("client-secret-value", encrypted);
        assertEquals("client-secret-value", crypto.decryptOrNull(encrypted));
    }

    @Test
    @DisplayName("decryptOrNull retorna null para ciphertext vazio ou inválido")
    void decryptOrNull_invalid() {
        OAuthClientSecretCrypto crypto = new OAuthClientSecretCrypto("base-key");
        assertNull(crypto.decryptOrNull(null));
        assertNull(crypto.decryptOrNull(""));
        assertNull(crypto.decryptOrNull("@@@"));
    }

    @Test
    @DisplayName("matchesBase compara em tempo constante")
    void matchesBase() {
        OAuthClientSecretCrypto crypto = new OAuthClientSecretCrypto("base-key");
        assertTrue(crypto.matchesBase("base-key"));
        assertFalse(crypto.matchesBase("other"));
        assertFalse(crypto.matchesBase(null));
    }

    @Test
    @DisplayName("rejeita BASE vazia")
    void rejectsBlankBase() {
        assertThrows(IllegalStateException.class, () -> new OAuthClientSecretCrypto(""));
    }
}
