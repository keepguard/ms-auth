package com.keepguard.ms_auth.infrastructure.config.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Gera o valor opaco do refresh token (256 bits aleatórios) e calcula o hash
 * usado como chave de busca no Redis. O valor opaco em si nunca é persistido
 * — só o hash, para que o vazamento do Redis não exponha tokens utilizáveis.
 */
@Component
public class RefreshTokenGenerator {

    private static final String PREFIX = "rt_";
    private static final int RANDOM_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOpaqueValue() {
        byte[] bytes = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(bytes);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String opaqueValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(opaqueValue.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }

    public String newFamilyId() {
        return "fam_" + UUID.randomUUID();
    }

    public String newSessionId() {
        return "sess_" + UUID.randomUUID();
    }
}
