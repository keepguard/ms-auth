package com.keepguard.ms_auth.infrastructure.config.security;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Service Tests")
class JwtServiceTest {

    @InjectMocks private JwtService jwtService;

    @Mock private HttpServletRequest request;

    private User user;
    private String secret = "mySecretKey123456789012345678901234567890";
    private long expiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        // Set private fields using reflection
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);
        
        // Initialize the service
        jwtService.init();
        
        user = UserTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCodeUser(UUID.randomUUID())
            .withUsername("testuser")
            .withEmail("test@example.com")
            .buildDomain();
    }

    @Test
    @DisplayName("Deve gerar token JWT com sucesso")
    void shouldGenerateTokenSuccessfully() {
        // Given
        List<String> roles = List.of("ADMIN", "USER");
        List<String> authorities = List.of("CREATE_USER", "DELETE_USER", "READ_USER");
        String application = "test-app";

        // When
        String token = jwtService.generateToken(user, roles, authorities, application, "Mozilla/5.0");

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token structure
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Deve gerar token JWT com request nulo")
    void shouldGenerateTokenWithNullRequest() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";

        // When
        String token = jwtService.generateToken(user, roles, authorities, application, null);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve gerar token JWT com roles vazias")
    void shouldGenerateTokenWithEmptyRoles() {
        // Given
        List<String> roles = List.of();
        List<String> authorities = new ArrayList<>();
        String application = "test-app";

        // When
        String token = jwtService.generateToken(user, roles, authorities, application, "Mozilla/5.0");

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve gerar token JWT com roles nulas")
    void shouldGenerateTokenWithNullRoles() {
        // Given
        String application = "test-app";
        List<String> authorities = new ArrayList<>();

        // When
        String token = jwtService.generateToken(user, null, authorities, application, "Mozilla/5.0");

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve validar token JWT válido")
    void shouldValidateValidToken() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";
        String token = jwtService.generateToken(user, roles, authorities, application, "Mozilla/5.0");

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve rejeitar token JWT inválido")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve rejeitar token JWT malformado")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "not.a.jwt";

        // When
        boolean isValid = jwtService.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve rejeitar token JWT vazio")
    void shouldRejectEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtService.validateToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve rejeitar token JWT nulo")
    void shouldRejectNullToken() {
        // When
        boolean isValid = jwtService.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve extrair ID do usuário do token")
    void shouldExtractUserIdFromToken() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";
        String token = jwtService.generateToken(user, roles, authorities, application, "Mozilla/5.0");

        // When
        UUID extractedUserId = jwtService.extractUserId(token);

        // Then
        assertNotNull(extractedUserId);
        assertEquals(user.getCodeUser(), extractedUserId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao extrair ID de token inválido")
    void shouldThrowExceptionWhenExtractingUserIdFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> jwtService.extractUserId(invalidToken));
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração correto")
    void shouldReturnCorrectExpiration() {
        // When
        long actualExpiration = jwtService.getExpiration();

        // Then
        assertEquals(expiration, actualExpiration);
    }

    @Test
    @DisplayName("Deve gerar token com claims corretos")
    void shouldGenerateTokenWithCorrectClaims() {
        // Given
        List<String> roles = List.of("ADMIN", "USER");
        List<String> authorities = List.of("CREATE_USER", "DELETE_USER", "READ_USER");
        String application = "test-app";
        String token = jwtService.generateToken(user, roles, authorities, application, "test-client");
 
        // When
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
 
        // Then
        assertEquals("ms-auth", claims.getIssuer());
        assertEquals("test-client", claims.getAudience().iterator().next());
        assertEquals(user.getCodeUser().toString(), claims.getSubject());
        assertEquals(roles, claims.get("roles", List.class));
        assertNotNull(claims.get("authorities", List.class));
        assertEquals("test-client", claims.get("client_id"));
        assertEquals("password", claims.get("login_method"));
        assertEquals(application, claims.get("tenant_id", String.class));
        assertNull(claims.get("companyId"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Deve gerar token de serviço com company_id e TTL do client")
    void shouldGenerateServiceTokenWithCompanyIdAndTtl() {
        UUID clientUuid = UUID.randomUUID();
        UUID companyId = UUID.fromString("f7fc7350-b9fc-4e54-9c58-ac9385b23ae4");
        long ttlMillis = 28800_000L;
        long before = System.currentTimeMillis();

        String token = jwtService.generateServiceToken(
                clientUuid, "investbot-collector", companyId, List.of("knowledge:write"), ttlMillis);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("ms-auth", claims.getIssuer());
        assertEquals(clientUuid.toString(), claims.getSubject());
        assertEquals("service", claims.get("token_type", String.class));
        assertEquals("client_credentials", claims.get("login_method", String.class));
        assertEquals(companyId.toString(), claims.get("company_id", String.class));
        assertEquals("investbot-collector", claims.get("client_id", String.class));
        assertEquals(List.of(), claims.get("roles", List.class));
        assertEquals(List.of("knowledge:write"), claims.get("authorities", List.class));
        long expDelta = claims.getExpiration().getTime() - before;
        assertTrue(expDelta >= ttlMillis - 2000);
        assertTrue(expDelta <= ttlMillis + 2000);
    }

    @Test
    @DisplayName("Deve gerar token de serviço com agent_id e agent_code opcionais")
    void shouldGenerateServiceTokenWithAgentClaims() {
        UUID clientUuid = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        UUID agentCode = UUID.randomUUID();

        String token = jwtService.generateServiceToken(
                clientUuid, "srv-data-collector", companyId, List.of("knowledge:write"), 3600_000L,
                agentId.toString(), agentCode.toString());

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(agentId.toString(), claims.get("agent_id", String.class));
        assertEquals(agentCode.toString(), claims.get("agent_code", String.class));
    }

    @Test
    @DisplayName("Deve gerar token de serviço com roles da service role")
    void shouldGenerateServiceTokenWithServiceRoles() {
        UUID clientUuid = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        String token = jwtService.generateServiceToken(
                clientUuid, "srv-data-collector", companyId,
                List.of("knowledge:read", "knowledge:write"), 3600_000L,
                null, null, List.of("ROLE_SERVICE_COLLECTOR"));

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(List.of("ROLE_SERVICE_COLLECTOR"), claims.get("roles", List.class));
        assertEquals(List.of("knowledge:read", "knowledge:write"), claims.get("authorities", List.class));
    }
 
    @Test
    @DisplayName("Deve detectar cliente Postman")
    void shouldDetectPostmanClient() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";
 
        // When
        String token = jwtService.generateToken(user, roles, authorities, application, "postman");
 
        // Then
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        assertEquals("postman", claims.get("client_id"));
    }
 
    @Test
    @DisplayName("Deve detectar cliente web")
    void shouldDetectWebClient() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";
 
        // When
        String token = jwtService.generateToken(user, roles, authorities, application, "web-app");
 
        // Then
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        assertEquals("web-app", claims.get("client_id"));
    }
 
    @Test
    @DisplayName("Deve detectar cliente mobile")
    void shouldDetectMobileClient() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";
 
        // When
        String token = jwtService.generateToken(user, roles, authorities, application, "mobile-app");
 
        // Then
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        assertEquals("mobile-app", claims.get("client_id"));
    }
 
    @Test
    @DisplayName("Deve retornar default para Client ID nulo")
    void shouldReturnDefaultClientForNullClientId() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";
 
        // When
        String token = jwtService.generateToken(user, roles, authorities, application, null);
 
        // Then
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        assertEquals("keepguard-default-client", claims.get("client_id"));
    }
 
    @Test
    @DisplayName("Deve retornar unknown para Client ID desconhecido")
    void shouldReturnUnknownForUnknownUserAgent() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";
 
        // When
        String token = jwtService.generateToken(user, roles, authorities, application, "unknown");
 
        // Then
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        assertEquals("unknown", claims.get("client_id"));
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para o mesmo usuário")
    void shouldGenerateDifferentTokensForSameUser() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";

        // When
        String token1 = jwtService.generateToken(user, roles, authorities, application, "Mozilla/5.0");
        String token2 = jwtService.generateToken(user, roles, authorities, application, "Mozilla/5.0");

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Deve gerar token com expiração futura")
    void shouldGenerateTokenWithFutureExpiration() {
        // Given
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";
        long beforeGeneration = System.currentTimeMillis();

        // When
        String token = jwtService.generateToken(user, roles, authorities, application, "Mozilla/5.0");

        // Then
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        long expirationTime = claims.getExpiration().getTime();
        long expectedExpiration = beforeGeneration + expiration;
        
        assertTrue(expirationTime > beforeGeneration);
        assertTrue(expirationTime >= expectedExpiration - 1000); // Allow 1 second tolerance
    }

    @Test
    @DisplayName("Deve gerar token com todas as authorities do usuário")
    @SuppressWarnings("unchecked")
    void shouldGenerateTokenWithAllAuthorities() {
        // Given
        List<String> roles = List.of("ADMIN", "USER");
        List<String> authorities = List.of("CREATE_USER", "DELETE_USER", "READ_USER", "UPDATE_USER");
        String application = "test-app";

        // When
        String token = jwtService.generateToken(user, roles, authorities, application, "Mozilla/5.0");

        // Then
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        List<String> extractedAuthorities = claims.get("authorities", List.class);
        assertNotNull(extractedAuthorities);
        assertEquals(4, extractedAuthorities.size());
        assertTrue(extractedAuthorities.contains("CREATE_USER"));
        assertTrue(extractedAuthorities.contains("DELETE_USER"));
        assertTrue(extractedAuthorities.contains("READ_USER"));
        assertTrue(extractedAuthorities.contains("UPDATE_USER"));
    }

    @Test
    @DisplayName("Não deve incluir display_handle no token")
    void shouldNotIncludeDisplayHandleInToken() {
        List<String> roles = List.of("ADMIN");
        List<String> authorities = List.of("READ_USER");
        String application = "test-app";

        String token = jwtService.generateToken(user, roles, authorities, application, "keepguard-web", "dev_123");

        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertNull(claims.get("display_handle"));
        assertEquals("dev_123", claims.get("device_id", String.class));
    }

    @Test
    @DisplayName("Deve sanitizar client_id com vírgula e não incluir companyId")
    void shouldSanitizeCommaClientIdAndOmitCompanyId() {
        String token = jwtService.generateToken(user, List.of("USER"), List.of("READ_USER"), "test-app", "keepguard-web, keepguard-web");

        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals("keepguard-web", claims.get("client_id"));
        assertEquals("keepguard-web", claims.getAudience().iterator().next());
        assertEquals("test-app", claims.get("tenant_id", String.class));
        assertNull(claims.get("companyId"));
    }
}
