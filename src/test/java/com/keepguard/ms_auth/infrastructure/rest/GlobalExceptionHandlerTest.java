package com.keepguard.ms_auth.infrastructure.rest;

import com.keepguard.lib_common.exception.InvalidEmailException;
import com.keepguard.lib_common.exception.InvalidPasswordException;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.EmailNotVerifiedException;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.RequiredFieldException;
import com.keepguard.ms_auth.application.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
    }

    @Test
    @DisplayName("Deve tratar EmailNotVerifiedException corretamente")
    void shouldHandleEmailNotVerifiedException() {
        // Given
        EmailNotVerifiedException ex = new EmailNotVerifiedException("Email não verificado");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleEmailNotVerified(ex, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), problemDetail.getStatus());
        assertEquals("Email não verificado", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/email-not-verified"), problemDetail.getType());
        assertEquals("Email não verificado", problemDetail.getTitle());
        assertEquals("EMAIL_NOT_VERIFIED", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar InvalidCredentialsException corretamente")
    void shouldHandleInvalidCredentialsException() {
        // Given
        InvalidCredentialsException ex = new InvalidCredentialsException("Credenciais inválidas");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleInvalidCredentials(ex, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), problemDetail.getStatus());
        assertEquals("Credenciais inválidas", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/invalid-credentials"), problemDetail.getType());
        assertEquals("Credenciais inválidas", problemDetail.getTitle());
        assertEquals("INVALID_CREDENTIALS", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException corretamente")
    void shouldHandleResourceNotFoundException() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso não encontrado");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleResourceNotFound(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Recurso não encontrado", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/not-found"), problemDetail.getType());
        assertEquals("Recurso não encontrado", problemDetail.getTitle());
        assertEquals("RESOURCE_NOT_FOUND", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar AlreadyExistsException corretamente")
    void shouldHandleAlreadyExistsException() {
        // Given
        AlreadyExistsException ex = new AlreadyExistsException("Recurso já existe");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleAlreadyExists(ex, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Recurso já existe", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/already-exists"), problemDetail.getType());
        assertEquals("Recurso já existe", problemDetail.getTitle());
        assertEquals("ALREADY_EXISTS", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar RequiredFieldException corretamente")
    void shouldHandleRequiredFieldException() {
        // Given
        RequiredFieldException ex = new RequiredFieldException("Campo obrigatório");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleRequiredField(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Campo obrigatório", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/required-field"), problemDetail.getType());
        assertEquals("Campo obrigatório", problemDetail.getTitle());
        assertEquals("REQUIRED_FIELD", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar InvalidEmailException corretamente")
    void shouldHandleInvalidEmailException() {
        // Given
        InvalidEmailException ex = new InvalidEmailException("Email inválido");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleInvalidEmail(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Email inválido", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/invalid-email"), problemDetail.getType());
        assertEquals("Email inválido", problemDetail.getTitle());
        assertEquals("INVALID_EMAIL", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar InvalidPasswordException corretamente")
    void shouldHandleInvalidPasswordException() {
        // Given
        InvalidPasswordException ex = new InvalidPasswordException("Senha inválida");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleInvalidPassword(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Senha inválida", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/invalid-password"), problemDetail.getType());
        assertEquals("Senha inválida", problemDetail.getTitle());
        assertEquals("INVALID_PASSWORD", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException corretamente")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleValidationErrors(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("field: defaultMessage", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/validation-error"), problemDetail.getType());
        assertEquals("Erro de validação", problemDetail.getTitle());
        assertEquals("VALIDATION_ERROR", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertEquals(1, problemDetail.getProperties().get("fieldErrors"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException corretamente")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Argumento ilegal");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Argumento ilegal", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/illegal-argument"), problemDetail.getType());
        assertEquals("Argumento ilegal", problemDetail.getTitle());
        assertEquals("ILLEGAL_ARGUMENT", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar Exception genérica corretamente")
    void shouldHandleGenericException() {
        // Given
        Exception ex = new Exception("Erro genérico");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleGenericException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Erro interno do servidor", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/internal-server-error"), problemDetail.getType());
        assertEquals("Erro interno do servidor", problemDetail.getTitle());
        assertEquals("INTERNAL_SERVER_ERROR", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException sem field errors")
    void shouldHandleMethodArgumentNotValidExceptionWithoutFieldErrors() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleValidationErrors(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProblemDetail problemDetail = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Erro de validação", problemDetail.getDetail());
        assertEquals(URI.create("https://keepguard.com/problems/validation-error"), problemDetail.getType());
        assertEquals("Erro de validação", problemDetail.getTitle());
        assertEquals("VALIDATION_ERROR", problemDetail.getProperties().get("errorCode"));
        assertEquals("/api/v1/test", problemDetail.getProperties().get("path"));
        assertEquals(0, problemDetail.getProperties().get("fieldErrors"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve incluir timestamp no formato correto")
    void shouldIncludeCorrectTimestampFormat() {
        // Given
        EmailNotVerifiedException ex = new EmailNotVerifiedException("Email não verificado");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleEmailNotVerified(ex, webRequest);

        // Then
        ProblemDetail problemDetail = response.getBody();
        String timestamp = (String) problemDetail.getProperties().get("timestamp");
        
        // Verifica se o timestamp está no formato ISO_LOCAL_DATE_TIME
        assertDoesNotThrow(() -> LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    @DisplayName("Deve processar path corretamente removendo 'uri='")
    void shouldProcessPathCorrectly() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/users/123");
        EmailNotVerifiedException ex = new EmailNotVerifiedException("Email não verificado");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleEmailNotVerified(ex, webRequest);

        // Then
        ProblemDetail problemDetail = response.getBody();
        assertEquals("/api/v1/users/123", problemDetail.getProperties().get("path"));
    }
}
