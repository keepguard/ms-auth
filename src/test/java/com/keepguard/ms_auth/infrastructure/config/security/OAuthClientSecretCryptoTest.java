package com.keepguard.ms_auth.infrastructure.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OAuthClientSecretCrypto")
class OAuthClientSecretCryptoTest {

    @Test
    @DisplayName("composeForHash é hex SHA-256 de 64 chars, independente do tamanho do BASE")
    void composeForHash_isSha256HexWithinBcryptLimit() {
        String longBase = "KgAuthClientSecretBase_7nR4wQ9pL2xH8mC3";
        OAuthClientSecretCrypto crypto = new OAuthClientSecretCrypto(longBase);
        String generated = "AnalystFinSecret_KeepGuard_32b!!";
        String material = crypto.composeForHash(generated);
        assertEquals(64, material.length());
        assertTrue(material.matches("[0-9a-f]{64}"));
        assertNotEquals(generated + longBase, material);
        assertEquals(generated + longBase, crypto.composeForHashLegacy(generated));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(material);
        assertTrue(encoder.matches(material, hash));
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
