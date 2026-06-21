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

    @Value("${security.jwt.expiration:3600000}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private static final Map<String, String> USER_AGENT_PATTERNS = Map.ofEntries(
            Map.entry("Postman", "postman"),
            Map.entry("Mozilla", "web-app"),
            Map.entry("Chrome", "web-app"),
            Map.entry("Safari", "web-app"),
            Map.entry("Mobile", "mobile-app"),
            Map.entry("Android", "mobile-app"),
            Map.entry("iPhone", "mobile-app")
    );

    public String generateToken(User user, List<String> roles, List<String> authorities, String xApplicationUuid, String userAgent) {
        return generateToken(user, roles, authorities, xApplicationUuid, userAgent, null);
    }

    public String generateToken(User user, List<String> roles, List<String> authorities, String xApplicationUuid, String userAgent, String displayHandle) {
        var builder = Jwts.builder()
                .issuer("ms-auth")
                .audience().add(xApplicationUuid).and()
                .id(UUID.randomUUID().toString())
                .subject(user.getCodeUser().toString())
                .claim("roles", roles)
                .claim("authorities", authorities)
                .claim("client_id", getUserAgent(userAgent))
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

    private String getUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "unknown";
        }

        String normalized = userAgent.toLowerCase();

        return USER_AGENT_PATTERNS.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey().toLowerCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("unknown");
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