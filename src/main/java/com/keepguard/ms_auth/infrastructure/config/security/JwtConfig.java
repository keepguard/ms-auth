package com.keepguard.ms_auth.infrastructure.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfig {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Converte a string secret para SecretKey
        SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");

        // Cria o JwtDecoder usando Nimbus
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
