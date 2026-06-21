package com.keepguard.ms_auth.infrastructure.rest;

import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.EmailNotVerifiedException;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.lib_common.exception.InvalidEmailException;
import com.keepguard.lib_common.exception.InvalidPasswordException;
import com.keepguard.lib_common.exception.InvalidXApplicationException;
import com.keepguard.ms_auth.application.service.exception.RequiredFieldException;
import com.keepguard.ms_auth.application.service.exception.ResourceNotFoundException;
import com.keepguard.ms_auth.application.service.exception.QueryOperationException;
import com.keepguard.ms_auth.application.service.exception.AccountLockedException;
import com.keepguard.ms_auth.application.service.exception.CommandOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ProblemDetail> handleEmailNotVerified(EmailNotVerifiedException ex, WebRequest request) {
        log.error("Email não verificado: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/email-not-verified"));
        problemDetail.setTitle("Email não verificado");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "EMAIL_NOT_VERIFIED");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String userAgent = request.getHeader("User-Agent");
        String correlationId = request.getHeader("X-Correlation-ID");
        
        log.error("Credenciais inválidas: message={}, path={}, correlationId={}, userAgent={}", 
            ex.getMessage(), path, correlationId, userAgent);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/invalid-credentials"));
        problemDetail.setTitle("Credenciais inválidas");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", path);
        problemDetail.setProperty("errorCode", "INVALID_CREDENTIALS");
        problemDetail.setProperty("correlationId", correlationId);
        problemDetail.setProperty("userAgent", userAgent);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler({ResourceNotFoundException.class, com.keepguard.ms_auth.application.service.exception.NotFoundException.class})
    public ResponseEntity<ProblemDetail> handleResourceNotFound(RuntimeException ex, WebRequest request) {
        log.error("Recurso não encontrado: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/not-found"));
        problemDetail.setTitle("Recurso não encontrado");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "RESOURCE_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAlreadyExists(AlreadyExistsException ex, WebRequest request) {
        log.error("Recurso já existe: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/already-exists"));
        problemDetail.setTitle("Recurso já existe");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "ALREADY_EXISTS");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(RequiredFieldException.class)
    public ResponseEntity<ProblemDetail> handleRequiredField(RequiredFieldException ex, WebRequest request) {
        log.error("Campo obrigatório: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/required-field"));
        problemDetail.setTitle("Campo obrigatório");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "REQUIRED_FIELD");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ProblemDetail> handleInvalidEmail(InvalidEmailException ex, WebRequest request) {
        log.error("Email inválido: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/invalid-email"));
        problemDetail.setTitle("Email inválido");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INVALID_EMAIL");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPassword(InvalidPasswordException ex, WebRequest request) {
        log.error("Senha inválida: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/invalid-password"));
        problemDetail.setTitle("Senha inválida");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INVALID_PASSWORD");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(InvalidXApplicationException.class)
    public ResponseEntity<ProblemDetail> handleInvalidXApplication(InvalidXApplicationException ex, WebRequest request) {
        log.error("Header X-Application inválido: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/invalid-x-application"));
        problemDetail.setTitle("Header X-Application inválido");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INVALID_X_APPLICATION");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Erro de validação");

        log.error("Erro de validação: {}", errorMessage);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setType(URI.create("https://keepguard.com/problems/validation-error"));
        problemDetail.setTitle("Erro de validação");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "VALIDATION_ERROR");
        problemDetail.setProperty("fieldErrors", ex.getBindingResult().getFieldErrors().size());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Argumento ilegal: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/illegal-argument"));
        problemDetail.setTitle("Argumento ilegal");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "ILLEGAL_ARGUMENT");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(QueryOperationException.class)
    public ResponseEntity<ProblemDetail> handleQueryOperationException(QueryOperationException ex, WebRequest request) {
        log.error("Falha na operação de consulta: {}", ex.getMessage());

        // Se a causa raiz for NotFoundException, retornar 404
        if (ex.getCause() instanceof com.keepguard.ms_auth.application.service.exception.NotFoundException) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getCause().getMessage());
            problemDetail.setType(URI.create("https://keepguard.com/problems/not-found"));
            problemDetail.setTitle("Recurso não encontrado");
            problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
            problemDetail.setProperty("errorCode", "RESOURCE_NOT_FOUND");
            problemDetail.setProperty("operation", ex.getOperation());
            problemDetail.setProperty("context", ex.getContext());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
        }

        // Para outros tipos de QueryOperationException, retornar 500
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/query-operation-failed"));
        problemDetail.setTitle("Falha na operação de consulta");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("operation", ex.getOperation());
        problemDetail.setProperty("context", ex.getContext());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ProblemDetail> handleAccountLockedException(AccountLockedException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String userAgent = request.getHeader("User-Agent");
        String correlationId = request.getHeader("X-Correlation-ID");
        
        log.error("Conta bloqueada: message={}, path={}, correlationId={}, userAgent={}", 
            ex.getMessage(), path, correlationId, userAgent);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.LOCKED, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/account-locked"));
        problemDetail.setTitle("Conta bloqueada");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", path);
        problemDetail.setProperty("errorCode", "ACCOUNT_LOCKED");
        problemDetail.setProperty("correlationId", correlationId);
        problemDetail.setProperty("userAgent", userAgent);

        return ResponseEntity.status(HttpStatus.LOCKED).body(problemDetail);
    }

    @ExceptionHandler(CommandOperationException.class)
    public ResponseEntity<ProblemDetail> handleCommandOperationException(CommandOperationException ex, WebRequest request) {
        log.error("Falha na operação de comando: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/command-operation-failed"));
        problemDetail.setTitle("Falha na operação de comando");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("operation", ex.getOperation());
        problemDetail.setProperty("context", ex.getContext());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {
        log.error("Erro interno do servidor: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
        problemDetail.setType(URI.create("https://keepguard.com/problems/internal-server-error"));
        problemDetail.setTitle("Erro interno do servidor");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}