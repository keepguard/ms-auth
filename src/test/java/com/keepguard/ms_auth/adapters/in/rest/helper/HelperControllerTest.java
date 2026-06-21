package com.keepguard.ms_auth.adapters.in.rest.helper;

import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthSimulateResetTokenRequestDTO;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para HelperController
 * Testa lógica do controller sem contexto Spring
 */
@ActiveProfiles("test")
@DisplayName("Helper Controller Tests")
class HelperControllerTest {
    
    @Mock
    private TokenCachePort tokenCachePort;
    
    private HelperController helperController;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        helperController = new HelperController(tokenCachePort);
    }
    
    @Test
    @DisplayName("Deve retornar health check com sucesso")
    void shouldReturnHealthCheckSuccessfully() {
        // When
        ResponseEntity<Map<String, String>> response = helperController.health();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("UP", responseBody.get("status"));
        assertEquals("Helper API", responseBody.get("service"));
        assertNotNull(responseBody.get("timestamp"));
    }
    
    @Test
    @DisplayName("Deve simular token de reset com token fornecido")
    void shouldSimulateResetTokenWithProvidedToken() {
        // Given
        String codeUser = "USR123456";
        String providedToken = "abc123def456ghi789";
        Long ttlMillis = 900000L; // 15 minutos
        
        AuthSimulateResetTokenRequestDTO request = new AuthSimulateResetTokenRequestDTO();
        request.setCodeUser(codeUser);
        request.setToken(providedToken);
        request.setTtlMillis(ttlMillis);
        
        doNothing().when(tokenCachePort).saveToken(anyString(), anyString(), anyString(), anyString(), anyLong());
        
        // When
        ResponseEntity<?> response = helperController.simulateResetToken(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(providedToken, responseBody.get("resetToken"));
        
        verify(tokenCachePort).saveToken(codeUser, "EMAIL", "RECUPERACAO_SENHA", providedToken, ttlMillis);
    }
    
    @Test
    @DisplayName("Deve simular token de reset com token gerado automaticamente")
    void shouldSimulateResetTokenWithGeneratedToken() {
        // Given
        String codeUser = "USR123456";
        Long ttlMillis = 1800000L; // 30 minutos
        
        AuthSimulateResetTokenRequestDTO request = new AuthSimulateResetTokenRequestDTO();
        request.setCodeUser(codeUser);
        request.setToken(null); // Token não fornecido
        request.setTtlMillis(ttlMillis);
        
        doNothing().when(tokenCachePort).saveToken(anyString(), anyString(), anyString(), anyString(), anyLong());
        
        // When
        ResponseEntity<?> response = helperController.simulateResetToken(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("resetToken"));
        assertFalse(responseBody.get("resetToken").isEmpty());
        
        verify(tokenCachePort).saveToken(eq(codeUser), eq("EMAIL"), eq("RECUPERACAO_SENHA"), anyString(), eq(ttlMillis));
    }
    
    @Test
    @DisplayName("Deve simular token de reset com token vazio (gerar automaticamente)")
    void shouldSimulateResetTokenWithEmptyToken() {
        // Given
        String codeUser = "USR123456";
        
        AuthSimulateResetTokenRequestDTO request = new AuthSimulateResetTokenRequestDTO();
        request.setCodeUser(codeUser);
        request.setToken(""); // Token vazio
        request.setTtlMillis(null); // TTL não fornecido
        
        doNothing().when(tokenCachePort).saveToken(anyString(), anyString(), anyString(), anyString(), anyLong());
        
        // When
        ResponseEntity<?> response = helperController.simulateResetToken(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("resetToken"));
        assertFalse(responseBody.get("resetToken").isEmpty());
        
        // Verifica se o TTL padrão foi usado (15 minutos = 900000 ms)
        verify(tokenCachePort).saveToken(eq(codeUser), eq("EMAIL"), eq("RECUPERACAO_SENHA"), anyString(), eq(900000L));
    }
    
    @Test
    @DisplayName("Deve simular token de reset com TTL padrão quando não fornecido")
    void shouldSimulateResetTokenWithDefaultTtl() {
        // Given
        String codeUser = "USR123456";
        String providedToken = "custom-token-123";
        
        AuthSimulateResetTokenRequestDTO request = new AuthSimulateResetTokenRequestDTO();
        request.setCodeUser(codeUser);
        request.setToken(providedToken);
        request.setTtlMillis(null); // TTL não fornecido
        
        doNothing().when(tokenCachePort).saveToken(anyString(), anyString(), anyString(), anyString(), anyLong());
        
        // When
        ResponseEntity<?> response = helperController.simulateResetToken(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(providedToken, responseBody.get("resetToken"));
        
        // Verifica se o TTL padrão foi usado (15 minutos = 900000 ms)
        verify(tokenCachePort).saveToken(eq(codeUser), eq("EMAIL"), eq("RECUPERACAO_SENHA"), eq(providedToken), eq(900000L));
    }
    
    @Test
    @DisplayName("Deve simular token de reset com token em branco (gerar automaticamente)")
    void shouldSimulateResetTokenWithWhitespaceToken() {
        // Given
        String codeUser = "USR123456";
        
        AuthSimulateResetTokenRequestDTO request = new AuthSimulateResetTokenRequestDTO();
        request.setCodeUser(codeUser);
        request.setToken("   "); // Token com espaços em branco
        request.setTtlMillis(600000L); // 10 minutos
        
        doNothing().when(tokenCachePort).saveToken(anyString(), anyString(), anyString(), anyString(), anyLong());
        
        // When
        ResponseEntity<?> response = helperController.simulateResetToken(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("resetToken"));
        assertFalse(responseBody.get("resetToken").isEmpty());
        
        verify(tokenCachePort).saveToken(eq(codeUser), eq("EMAIL"), eq("RECUPERACAO_SENHA"), anyString(), eq(600000L));
    }
    
    @Test
    @DisplayName("Deve simular token de reset com diferentes TTLs")
    void shouldSimulateResetTokenWithDifferentTtls() {
        // Given
        String codeUser = "USR123456";
        String providedToken = "test-token-123";
        
        // Teste com 5 minutos
        AuthSimulateResetTokenRequestDTO request5min = new AuthSimulateResetTokenRequestDTO();
        request5min.setCodeUser(codeUser);
        request5min.setToken(providedToken);
        request5min.setTtlMillis(300000L); // 5 minutos
        
        doNothing().when(tokenCachePort).saveToken(anyString(), anyString(), anyString(), anyString(), anyLong());
        
        // When
        ResponseEntity<?> response = helperController.simulateResetToken(request5min);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenCachePort).saveToken(eq(codeUser), eq("EMAIL"), eq("RECUPERACAO_SENHA"), eq(providedToken), eq(300000L));
        
        // Teste com 1 hora
        AuthSimulateResetTokenRequestDTO request1hour = new AuthSimulateResetTokenRequestDTO();
        request1hour.setCodeUser(codeUser);
        request1hour.setToken(providedToken);
        request1hour.setTtlMillis(3600000L); // 1 hora
        
        // When
        ResponseEntity<?> response1hour = helperController.simulateResetToken(request1hour);
        
        // Then
        assertEquals(HttpStatus.OK, response1hour.getStatusCode());
        verify(tokenCachePort).saveToken(eq(codeUser), eq("EMAIL"), eq("RECUPERACAO_SENHA"), eq(providedToken), eq(3600000L));
    }
    
    @Test
    @DisplayName("Deve simular token de reset com código de usuário válido")
    void shouldSimulateResetTokenWithValidUserCode() {
        // Given
        String codeUser = "USR789012";
        String providedToken = "valid-token-456";
        Long ttlMillis = 1200000L; // 20 minutos
        
        AuthSimulateResetTokenRequestDTO request = new AuthSimulateResetTokenRequestDTO();
        request.setCodeUser(codeUser);
        request.setToken(providedToken);
        request.setTtlMillis(ttlMillis);
        
        doNothing().when(tokenCachePort).saveToken(anyString(), anyString(), anyString(), anyString(), anyLong());
        
        // When
        ResponseEntity<?> response = helperController.simulateResetToken(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(providedToken, responseBody.get("resetToken"));
        
        verify(tokenCachePort).saveToken(eq(codeUser), eq("EMAIL"), eq("RECUPERACAO_SENHA"), eq(providedToken), eq(ttlMillis));
    }
    
    @Test
    @DisplayName("Deve simular token de reset com UUID gerado automaticamente")
    void shouldSimulateResetTokenWithGeneratedUuid() {
        // Given
        String codeUser = "USR123456";
        
        AuthSimulateResetTokenRequestDTO request = new AuthSimulateResetTokenRequestDTO();
        request.setCodeUser(codeUser);
        request.setToken(null);
        request.setTtlMillis(900000L);
        
        doNothing().when(tokenCachePort).saveToken(anyString(), anyString(), anyString(), anyString(), anyLong());
        
        // When
        ResponseEntity<?> response = helperController.simulateResetToken(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        String generatedToken = responseBody.get("resetToken");
        assertNotNull(generatedToken);
        assertFalse(generatedToken.isEmpty());
        
        // Verifica se é um UUID válido
        assertDoesNotThrow(() -> UUID.fromString(generatedToken));
        
        verify(tokenCachePort).saveToken(eq(codeUser), eq("EMAIL"), eq("RECUPERACAO_SENHA"), eq(generatedToken), eq(900000L));
    }
}
