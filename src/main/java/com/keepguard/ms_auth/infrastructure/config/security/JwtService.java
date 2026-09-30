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
import java.util.UUID;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    @Value("${security.jwt.access-expiration:900000}")
    private long accessExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user, List<String> roles, List<String> authorities, String tenantId, String clientId) {
        return generateToken(user, roles, authorities, tenantId, clientId, null, null);
    }

    public String generateToken(User user, List<String> roles, List<String> authorities, String tenantId, String clientId, String deviceId) {
        return generateToken(user, roles, authorities, tenantId, clientId, deviceId, null);
    }

    /**
     * Gera o access token. Quando {@code sid} é informado, o token carrega a
     * claim de sessão usada pela revogação por sessão (em vez de por token
     * exato) — ver bff-core jwt_middleware.
     */
    public String generateToken(User user, List<String> roles, List<String> authorities, String tenantId,
                                 String clientId, String deviceId, String sid) {
        String finalClientId = sanitizeClientId(clientId);
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

        if (deviceId != null && !deviceId.isBlank()) {
            builder.claim("device_id", deviceId);
        }
        if (sid != null && !sid.isBlank()) {
            builder.claim("sid", sid);
        }

        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateServiceToken(UUID clientUuid, String clientId, UUID companyId,
                                       List<String> authorities, long ttlMillis) {
        return generateServiceToken(clientUuid, clientId, companyId, authorities, ttlMillis, null, null, List.of());
    }

    public String generateServiceToken(UUID clientUuid, String clientId, UUID companyId,
                                       List<String> authorities, long ttlMillis,
                                       String agentId, String agentCode) {
        return generateServiceToken(clientUuid, clientId, companyId, authorities, ttlMillis, agentId, agentCode, List.of());
    }

    public String generateServiceToken(UUID clientUuid, String clientId, UUID companyId,
                                       List<String> authorities, long ttlMillis,
                                       String agentId, String agentCode, List<String> roles) {
        String finalClientId = sanitizeClientId(clientId);
        List<String> tokenAuthorities = authorities == null ? List.of() : List.copyOf(authorities);
        List<String> tokenRoles = roles == null ? List.of() : List.copyOf(roles);
        var builder = Jwts.builder()
                .issuer("ms-auth")
                .audience().add(finalClientId).and()
                .id(UUID.randomUUID().toString())
                .subject(clientUuid.toString())
                .claim("roles", tokenRoles)
                .claim("authorities", tokenAuthorities)
                .claim("client_id", finalClientId)
                .claim("company_id", companyId.toString())
                .claim("token_type", "service")
                .claim("login_method", "client_credentials");

        if (agentId != null && !agentId.isBlank()) {
            builder.claim("agent_id", agentId.trim());
        }
        if (agentCode != null && !agentCode.isBlank()) {
            builder.claim("agent_code", agentCode.trim());
        }

        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
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

    /**
     * Extrai o codeUser mesmo de um access token já expirado — usado no fluxo
     * de refresh opaco, onde a credencial de rotação é o refresh token, não o
     * JWT (que serve só para identificar de quem é a sessão). A assinatura
     * ainda é verificada; só a expiração é tolerada.
     */
    public UUID extractUserIdIgnoringExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (ExpiredJwtException e) {
            return UUID.fromString(e.getClaims().getSubject());
        }
    }

    public String extractDeviceId(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.get("device_id", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String extractSessionId(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.get("sid", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public long getExpiration() {
        return expiration;
    }

    public long getAccessExpiration() {
        return accessExpiration;
    }

    private String sanitizeClientId(String clientId) {
        String value = (clientId == null || clientId.isBlank()) ? "keepguard-default-client" : clientId.trim();
        if (value.contains(",")) {
            String first = value.split(",")[0].trim();
            return first.isBlank() ? "keepguard-default-client" : first;
        }
        return value;
    }
}