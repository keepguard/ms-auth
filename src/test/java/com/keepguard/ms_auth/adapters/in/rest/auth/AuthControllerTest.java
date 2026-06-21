package com.keepguard.ms_auth.adapters.in.rest.auth;

import com.keepguard.ms_auth.adapters.in.rest.auth.dto.*;
import com.keepguard.ms_auth.application.port.in.AuthPort;
import com.keepguard.ms_auth.adapters.in.rest.auth.mapper.AuthAdapterMapper;
import com.keepguard.ms_auth.domain.dto.auth.AuthLoginCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthRefreshTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthLogoutCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthValidateTokenQueryDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthChangePasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthResetPasswordCommandDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AuthController
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    
    @Mock
    private AuthPort authService;
    
    @Mock
    private AuthAdapterMapper authAdapterMapper;
    
    @InjectMocks
    private AuthController authController;
    
    private AuthLoginRequestDTO authRequest;
    private com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthRefreshTokenRequestDTO refreshTokenRequest;
    private AuthValidateTokenRequestDTO validateTokenRequest;
    private AuthChangePasswordRequestDTO changePasswordRequest;
    private AuthResetPasswordRequestDTO resetPasswordRequest;
    
    @BeforeEach
    void setUp() {
        authRequest = AuthLoginRequestDTO.builder()
            .username("testuser")
            .password("password123")
            .build();
        
        
        refreshTokenRequest = com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthRefreshTokenRequestDTO.builder()
            .token("jwt-token")
            .build();
        
        validateTokenRequest = AuthValidateTokenRequestDTO.builder()
            .token("eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJtcy1hdXRoIiwiYXVkIjpbIjVlYzNmY2EyLWZlZWYtNDA5NC05MzFhLTk4OTkyYjg0NTM2YyJdLCJqdGkiOiI2NDEwM2NhZC1hODQxLTRjNTYtOThkYi0yOGZiMzNjODgyMDUiLCJzdWIiOiI4ZDU4MmE5ZC03OTUxLTQxZDMtOGVlNi0zMGIyOGZkMmRjZDQiLCJyb2xlcyI6W10sImNsaWVudF9pZCI6IndlYi1hcHAiLCJsb2dpbl9tZXRob2QiOiJwYXNzd29yZCIsImlhdCI6MTc1OTQ0MzQxMSwiZXhwIjoxNzU5NDQ3MDExfQ.hz-yx2Il4Me6dETke3hjV6sCFQ_pBPHXBBJ1ennM6zg")
            .build();
        
        changePasswordRequest = new AuthChangePasswordRequestDTO();
        changePasswordRequest.setCodeUser("code-123");
        changePasswordRequest.setCurrentPassword("oldpassword");
        changePasswordRequest.setNewPassword("newpassword123");
        changePasswordRequest.setConfirmNewPassword("newpassword123");
        
        resetPasswordRequest = new AuthResetPasswordRequestDTO();
        resetPasswordRequest.setCodeUser("code-123");
        resetPasswordRequest.setResetToken("reset-token");
        resetPasswordRequest.setNewPassword("newpassword123");
        resetPasswordRequest.setConfirmNewPassword("newpassword123");
    }
    
    @Test
    @DisplayName("Deve realizar login com sucesso")
    void shouldLoginSuccessfully() {
        // Given
        String expectedToken = "jwt-token";
        var expectedView = new com.keepguard.ms_auth.application.dto.auth.AuthLoginView(expectedToken, 3600L);
        
        when(authAdapterMapper.toLoginCommand(any(), any(), any())).thenReturn(AuthLoginCommandDTO.builder().build());
        when(authService.login(any(AuthLoginCommandDTO.class))).thenReturn(expectedView);
        when(authAdapterMapper.toLoginResponseDTO(any(com.keepguard.ms_auth.application.dto.auth.AuthLoginView.class))).thenReturn(AuthLoginResponseDTO.builder()
            .token(expectedToken)
            .expiresIn(3600L)
            .build());
        
        // When
        ResponseEntity<AuthLoginResponseDTO> response = authController.login(authRequest, "550e8400-e29b-41d4-a716-446655440000", "Mozilla/5.0");
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        AuthLoginResponseDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedToken, responseBody.getToken());
        assertEquals(3600L, responseBody.getExpiresIn());
        
        verify(authService, times(1)).login(any(AuthLoginCommandDTO.class));
    }
    
    @Test
    @DisplayName("Deve renovar token com sucesso")
    void shouldRefreshTokenSuccessfully() {
        // Given
        String expectedToken = "new-jwt-token";
        var expectedView = new com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenView(expectedToken, 3600L);
        
        when(authAdapterMapper.toRefreshTokenCommand(any(), any(), any())).thenReturn(AuthRefreshTokenCommandDTO.builder().build());
        when(authService.refreshToken(any(AuthRefreshTokenCommandDTO.class))).thenReturn(expectedView);
        when(authAdapterMapper.toRefreshTokenResponseDTO(any(com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenView.class))).thenReturn(AuthRefreshTokenResponseDTO.builder()
            .token(expectedToken)
            .expiresIn(3600L)
            .build());
        
        // When
        ResponseEntity<AuthRefreshTokenResponseDTO> response = authController.refreshToken(refreshTokenRequest, "550e8400-e29b-41d4-a716-446655440000", "Mozilla/5.0");
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        AuthRefreshTokenResponseDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedToken, responseBody.getToken());
        assertEquals(3600L, responseBody.getExpiresIn());
        
        verify(authService, times(1)).refreshToken(any(AuthRefreshTokenCommandDTO.class));
    }
    
    @Test
    @DisplayName("Deve realizar logout com sucesso")
    void shouldLogoutSuccessfully() {
        // Given
        String token = "Bearer jwt-token";
        var expectedView = new com.keepguard.ms_auth.application.dto.auth.AuthLogoutView("Logout realizado com sucesso", true);
        
        when(authAdapterMapper.toLogoutCommand(any(), any())).thenReturn(AuthLogoutCommandDTO.builder().build());
        when(authService.logout(any(AuthLogoutCommandDTO.class))).thenReturn(expectedView);
        when(authAdapterMapper.toLogoutResponseDTO(any(com.keepguard.ms_auth.application.dto.auth.AuthLogoutView.class))).thenReturn(AuthLogoutResponseDTO.builder()
            .message("Logout realizado com sucesso")
            .success(true)
            .build());
        
        // When
        ResponseEntity<AuthLogoutResponseDTO> response = authController.logout(token, "550e8400-e29b-41d4-a716-446655440000");
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout realizado com sucesso", response.getBody().getMessage());
        assertTrue(response.getBody().isSuccess());
        
        verify(authService, times(1)).logout(any(AuthLogoutCommandDTO.class));
    }
    
    
    @Test
    @DisplayName("Deve lidar com exceções durante o login")
    void shouldHandleExceptionsDuringLogin() {
        // Given
        when(authAdapterMapper.toLoginCommand(any(), any(), any())).thenReturn(AuthLoginCommandDTO.builder().build());
        when(authService.login(any(AuthLoginCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.login(authRequest, "550e8400-e29b-41d4-a716-446655440000", "Mozilla/5.0");
        });
        
        verify(authService, times(1)).login(any(AuthLoginCommandDTO.class));
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante refresh token")
    void shouldHandleExceptionsDuringRefreshToken() {
        // Given
        when(authAdapterMapper.toRefreshTokenCommand(any(), any(), any())).thenReturn(AuthRefreshTokenCommandDTO.builder().build());
        when(authService.refreshToken(any(AuthRefreshTokenCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.refreshToken(refreshTokenRequest, "550e8400-e29b-41d4-a716-446655440000", "Mozilla/5.0");
        });
        
        verify(authService, times(1)).refreshToken(any(AuthRefreshTokenCommandDTO.class));
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante logout")
    void shouldHandleExceptionsDuringLogout() {
        // Given
        String token = "Bearer jwt-token";
        when(authAdapterMapper.toLogoutCommand(any(), any())).thenReturn(AuthLogoutCommandDTO.builder().build());
        doThrow(new RuntimeException("Service error")).when(authService).logout(any(AuthLogoutCommandDTO.class));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.logout(token, "550e8400-e29b-41d4-a716-446655440000");
        });
        
        verify(authService, times(1)).logout(any(AuthLogoutCommandDTO.class));
    }
    
    @Test
    @DisplayName("Deve validar token com sucesso")
    void shouldValidateTokenSuccessfully() {
        // Given
        when(authAdapterMapper.toValidateTokenCommand(any(), any())).thenReturn(AuthValidateTokenQueryDTO.builder().build());
        doNothing().when(authService).validateToken(any(AuthValidateTokenQueryDTO.class));
        
        // When
        ResponseEntity<Void> response = authController.validate(validateTokenRequest, "550e8400-e29b-41d4-a716-446655440000");
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(authService, times(1)).validateToken(any(AuthValidateTokenQueryDTO.class));
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante validação de token")
    void shouldHandleExceptionsDuringValidateToken() {
        // Given
        when(authAdapterMapper.toValidateTokenCommand(any(), any())).thenReturn(AuthValidateTokenQueryDTO.builder().build());
        doThrow(new RuntimeException("Token inválido")).when(authService).validateToken(any(AuthValidateTokenQueryDTO.class));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.validate(validateTokenRequest, "550e8400-e29b-41d4-a716-446655440000");
        });
        
        verify(authService, times(1)).validateToken(any(AuthValidateTokenQueryDTO.class));
    }
    
    @Test
    @DisplayName("Deve alterar senha com sucesso")
    void shouldChangePasswordSuccessfully() {
        // Given
        when(authAdapterMapper.toChangePasswordCommand(any(), any())).thenReturn(AuthChangePasswordCommandDTO.builder().build());
        doNothing().when(authService).changePassword(any(AuthChangePasswordCommandDTO.class));
        
        // When
        ResponseEntity<Void> response = authController.changePassword(changePasswordRequest, "550e8400-e29b-41d4-a716-446655440000");
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(authService, times(1)).changePassword(any(AuthChangePasswordCommandDTO.class));
    }
    
    @Test
    @DisplayName("Deve resetar senha com sucesso")
    void shouldResetPasswordSuccessfully() {
        // Given
        when(authAdapterMapper.toResetPasswordCommand(any(), any())).thenReturn(AuthResetPasswordCommandDTO.builder().build());
        doNothing().when(authService).resetPassword(any(AuthResetPasswordCommandDTO.class));
        
        // When
        ResponseEntity<Void> response = authController.resetPassword(resetPasswordRequest, "550e8400-e29b-41d4-a716-446655440000");
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(authService, times(1)).resetPassword(any(AuthResetPasswordCommandDTO.class));
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante alteração de senha")
    void shouldHandleExceptionsDuringChangePassword() {
        // Given
        when(authAdapterMapper.toChangePasswordCommand(any(), any())).thenReturn(AuthChangePasswordCommandDTO.builder().build());
        doThrow(new RuntimeException("Service error")).when(authService).changePassword(any(AuthChangePasswordCommandDTO.class));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.changePassword(changePasswordRequest, "550e8400-e29b-41d4-a716-446655440000");
        });
        
        verify(authService, times(1)).changePassword(any(AuthChangePasswordCommandDTO.class));
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante reset de senha")
    void shouldHandleExceptionsDuringResetPassword() {
        // Given
        when(authAdapterMapper.toResetPasswordCommand(any(), any())).thenReturn(AuthResetPasswordCommandDTO.builder().build());
        doThrow(new RuntimeException("Service error")).when(authService).resetPassword(any(AuthResetPasswordCommandDTO.class));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.resetPassword(resetPasswordRequest, "550e8400-e29b-41d4-a716-446655440000");
        });
        
        verify(authService, times(1)).resetPassword(any(AuthResetPasswordCommandDTO.class));
    }
    
}
