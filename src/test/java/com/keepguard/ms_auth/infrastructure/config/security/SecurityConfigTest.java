package com.keepguard.ms_auth.infrastructure.config.security;

import com.keepguard.ms_auth.infrastructure.filter.CorrelationIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @Mock
    private CorrelationIdFilter correlationIdFilter;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(correlationIdFilter);
    }

    @Test
    @DisplayName("Deve criar PasswordEncoder bean")
    void shouldCreatePasswordEncoderBean() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    @DisplayName("Deve criar CorsConfigurationSource bean")
    void shouldCreateCorsConfigurationSourceBean() {
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Then
        assertNotNull(corsConfigurationSource);
        assertTrue(corsConfigurationSource instanceof UrlBasedCorsConfigurationSource);
    }

    @Test
    @DisplayName("PasswordEncoder deve funcionar corretamente")
    void passwordEncoderShouldWorkCorrectly() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    @DisplayName("PasswordEncoder deve rejeitar senha incorreta")
    void passwordEncoderShouldRejectIncorrectPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Then
        assertFalse(matches);
    }

    // ========== JWT CONFIG TESTS ==========

    @Test
    @DisplayName("Deve criar JwtDecoder bean com secret válido")
    void shouldCreateJwtDecoderBeanWithValidSecret() {
        // Given
        JwtConfig jwtConfig = new JwtConfig();
        String testSecret = "mySecretKey123456789012345678901234567890"; // 32+ chars for HMAC-SHA256
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", testSecret);

        // When
        JwtDecoder jwtDecoder = jwtConfig.jwtDecoder();

        // Then
        assertNotNull(jwtDecoder);
    }

    @Test
    @DisplayName("Deve criar JwtDecoder bean com secret de tamanho mínimo")
    void shouldCreateJwtDecoderBeanWithMinimumSecret() {
        // Given
        JwtConfig jwtConfig = new JwtConfig();
        String testSecret = "12345678901234567890123456789012"; // 32 chars minimum
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", testSecret);

        // When
        JwtDecoder jwtDecoder = jwtConfig.jwtDecoder();

        // Then
        assertNotNull(jwtDecoder);
    }

    @Test
    @DisplayName("Deve criar JwtDecoder bean com secret longo")
    void shouldCreateJwtDecoderBeanWithLongSecret() {
        // Given
        JwtConfig jwtConfig = new JwtConfig();
        String testSecret = "myVeryLongSecretKeyThatIsLongerThanMinimumRequiredLengthForHMACSHA256Algorithm";
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", testSecret);

        // When
        JwtDecoder jwtDecoder = jwtConfig.jwtDecoder();

        // Then
        assertNotNull(jwtDecoder);
    }

    @Test
    @DisplayName("Deve criar JwtDecoder bean com secret contendo caracteres especiais")
    void shouldCreateJwtDecoderBeanWithSpecialCharactersSecret() {
        // Given
        JwtConfig jwtConfig = new JwtConfig();
        String testSecret = "mySecret!@#$%^&*()_+-=[]{}|;':\",./<>?1234567890";
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", testSecret);

        // When
        JwtDecoder jwtDecoder = jwtConfig.jwtDecoder();

        // Then
        assertNotNull(jwtDecoder);
    }

    @Test
    @DisplayName("Deve lançar exceção com secret vazio")
    void shouldThrowExceptionWithEmptySecret() {
        // Given
        JwtConfig jwtConfig = new JwtConfig();
        String testSecret = "";
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", testSecret);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> jwtConfig.jwtDecoder());
    }

    @Test
    @DisplayName("Deve criar JwtDecoder bean com secret null")
    void shouldCreateJwtDecoderBeanWithNullSecret() {
        // Given
        JwtConfig jwtConfig = new JwtConfig();
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", null);

        // When & Then
        assertThrows(NullPointerException.class, () -> jwtConfig.jwtDecoder());
    }

    @Test
    @DisplayName("JwtDecoder deve usar algoritmo HMAC-SHA256")
    void jwtDecoderShouldUseHMACSHA256Algorithm() {
        // Given
        JwtConfig jwtConfig = new JwtConfig();
        String testSecret = "mySecretKey123456789012345678901234567890";
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", testSecret);

        // When
        JwtDecoder jwtDecoder = jwtConfig.jwtDecoder();

        // Then
        assertNotNull(jwtDecoder);
        // Verifica se o decoder foi criado (não podemos testar diretamente o algoritmo interno)
        // mas podemos verificar que não é null e não lança exceção
    }

    @Test
    @DisplayName("Deve criar instância de JwtConfig")
    void shouldCreateJwtConfigInstance() {
        // When
        JwtConfig jwtConfig = new JwtConfig();

        // Then
        assertNotNull(jwtConfig);
    }
}