package com.keepguard.ms_auth.infrastructure.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Hash: BCrypt(plain + AUTH_CLIENT_SECRET_BASE).
 * Persistência recuperável: AES-256-GCM, chave = SHA-256(BASE).
 * Payload: Base64(IV 12 bytes || ciphertext || tag 16 bytes).
 */
@Component
public class OAuthClientSecretCrypto {

    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final String secretBase;
    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthClientSecretCrypto(
            @Value("${security.oauth.client-secret-base:}") String secretBase) {
        if (!StringUtils.hasText(secretBase)) {
            throw new IllegalStateException(
                    "AUTH_CLIENT_SECRET_BASE (security.oauth.client-secret-base) é obrigatório.");
        }
        this.secretBase = secretBase;
        this.key = new SecretKeySpec(sha256(secretBase), "AES");
    }

    public String composeForHash(String plainSecret) {
        if (plainSecret == null) {
            return secretBase;
        }
        return plainSecret + secretBase;
    }

    public boolean matchesBase(String presented) {
        if (presented == null) {
            return false;
        }
        byte[] expected = secretBase.getBytes(StandardCharsets.UTF_8);
        byte[] actual = presented.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    public String encrypt(String plainSecret) {
        if (plainSecret == null || plainSecret.isEmpty()) {
            throw new IllegalArgumentException("clientSecret é obrigatório para cifrar.");
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainSecret.getBytes(StandardCharsets.UTF_8));
            byte[] packed = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(cipherText, 0, packed, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(packed);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao cifrar clientSecret.", ex);
        }
    }

    public String decryptOrNull(String encrypted) {
        if (!StringUtils.hasText(encrypted)) {
            return null;
        }
        try {
            byte[] packed = Base64.getDecoder().decode(encrypted);
            if (packed.length <= IV_LENGTH) {
                return null;
            }
            byte[] iv = Arrays.copyOfRange(packed, 0, IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(packed, IV_LENGTH, packed.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return null;
        }
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 indisponível.", ex);
        }
    }
}
