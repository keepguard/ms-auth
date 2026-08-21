package com.keepguard.ms_auth.infrastructure.config.security;

import com.keepguard.ms_auth.domain.entity.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user, List<String> roles, List<String> authorities, String tenantId, String clientId) {
        return generateToken(user, roles, authorities, tenantId, clientId, null);
    }

    public String generateToken(User user, List<String> roles, List<String> authorities, String tenantId, String clientId, String displayHandle) {
        String finalClientId = (clientId == null || clientId.isBlank()) ? "keepguard-default-client" : clientId;
        var builder = Jwts.builder()
                .issuer("ms-auth")
                .audience().add(finalClientId).and()
                .id(UUID.randomUUID().toString())
                .subject(user.getCodeUser().toString())
                .claim("roles", roles)
                .claim("authorities", authorities)
                .claim("client_id", finalClientId)
                .claim("tenant_id", tenantId)
                .claim("login_method", "password");
        
        if (displayHandle != null && !displayHandle.isEmpty()) {
            builder.claim("display_handle", displayHandle);
        }
        
        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }



    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return UUID.fromString(claims.getSubject());
    }

    public long getExpiration() {
        return expiration;
    }
}