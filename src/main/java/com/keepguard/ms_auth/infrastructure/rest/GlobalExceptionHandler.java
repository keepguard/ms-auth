package com.keepguard.ms_auth.infrastructure.rest;

import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.EmailNotVerifiedException;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.lib_common.exception.InvalidEmailException;
import com.keepguard.lib_common.exception.InvalidPasswordException;
import com.keepguard.lib_common.exception.InvalidTenantIdException;
import com.keepguard.ms_auth.application.service.exception.RequiredFieldException;
import com.keepguard.ms_auth.application.service.exception.ResourceNotFoundException;
import com.keepguard.ms_auth.application.service.exception.QueryOperationException;
import com.keepguard.ms_auth.application.service.exception.AccountLockedException;
import com.keepguard.ms_auth.application.service.exception.CommandOperationException;
import com.keepguard.ms_auth.application.service.exception.DeviceBlacklistedException;
import com.keepguard.ms_auth.application.service.exception.ConflictException;
import com.keepguard.ms_auth.application.service.exception.CompanyDefaultRolesNotConfiguredException;
import com.keepguard.ms_auth.infrastructure.context.CorrelationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

    private final CorrelationContext correlationContext;

    private String resolveCorrelationId(WebRequest request) {
        String fromContext = correlationContext != null ? correlationContext.getCorrelationId() : null;
        if (fromContext != null && !fromContext.isBlank()) {
            return fromContext;
        }
        return request.getHeader("X-Correlation-ID");
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ProblemDetail> handleEmailNotVerified(EmailNotVerifiedException ex, WebRequest request) {
        log.error("Email não verificado: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/email-not-verified"));
        problemDetail.setTitle("Email não verificado");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "EMAIL_NOT_VERIFIED");
        problemDetail.setProperty("correlationId", resolveCorrelationId(request));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(DeviceBlacklistedException.class)
    public ResponseEntity<ProblemDetail> handleDeviceBlacklisted(DeviceBlacklistedException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String clientId = request.getHeader("X-Client-ID");
        String correlationId = resolveCorrelationId(request);

        log.warn("Dispositivo bloqueado na blacklist: message={}, path={}, correlationId={}, clientId={}",
                ex.getMessage(), path, correlationId, clientId);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/device-blacklisted"));
        problemDetail.setTitle("Dispositivo bloqueado");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", path);
        problemDetail.setProperty("errorCode", "DEVICE_BLACKLISTED");
        problemDetail.setProperty("correlationId", correlationId);
        problemDetail.setProperty("userAgent", clientId);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String clientId = request.getHeader("X-Client-ID");
        String correlationId = resolveCorrelationId(request);
        
        log.error("Credenciais inválidas: message={}, path={}, correlationId={}, clientId={}", 
            ex.getMessage(), path, correlationId, clientId);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/invalid-credentials"));
        problemDetail.setTitle("Credenciais inválidas");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", path);
        problemDetail.setProperty("errorCode", "INVALID_CREDENTIALS");
        problemDetail.setProperty("correlationId", correlationId);
        problemDetail.setProperty("userAgent", clientId);

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

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex, WebRequest request) {
        log.warn("Conflito de negócio: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/conflict"));
        problemDetail.setTitle("Conflito");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", ex.getErrorCode());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(CompanyDefaultRolesNotConfiguredException.class)
    public ResponseEntity<ProblemDetail> handleCompanyDefaultRolesNotConfigured(
            CompanyDefaultRolesNotConfiguredException ex, WebRequest request) {
        log.warn("Company sem roles default: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/company-default-roles-not-configured"));
        problemDetail.setTitle("Roles default da company não configuradas");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("companyId", ex.getCompanyId());

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

    @ExceptionHandler(InvalidTenantIdException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTenantId(InvalidTenantIdException ex, WebRequest request) {
        log.error("Header X-Tenant-Id inválido: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/invalid-x-tenant-id"));
        problemDetail.setTitle("Header X-Tenant-Id inválido");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INVALID_TENANT_ID");

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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/access-denied"));
        problemDetail.setTitle("Acesso negado");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "ACCESS_DENIED");
        problemDetail.setProperty("correlationId", resolveCorrelationId(request));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
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
        String clientId = request.getHeader("X-Client-ID");
        String correlationId = resolveCorrelationId(request);
        
        log.error("Conta bloqueada: message={}, path={}, correlationId={}, clientId={}", 
            ex.getMessage(), path, correlationId, clientId);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.LOCKED, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/account-locked"));
        problemDetail.setTitle("Conta bloqueada");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", path);
        problemDetail.setProperty("errorCode", "ACCOUNT_LOCKED");
        problemDetail.setProperty("correlationId", correlationId);
        problemDetail.setProperty("userAgent", clientId);

        return ResponseEntity.status(HttpStatus.LOCKED).body(problemDetail);
    }

    @ExceptionHandler(com.keepguard.ms_auth.application.service.exception.ForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleForbiddenException(
            com.keepguard.ms_auth.application.service.exception.ForbiddenException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String correlationId = resolveCorrelationId(request);

        log.warn("Acesso negado: message={}, path={}, errorCode={}, correlationId={}",
                ex.getMessage(), path, ex.getErrorCode(), correlationId);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/forbidden"));
        problemDetail.setTitle("Acesso negado");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", path);
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("correlationId", correlationId);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(com.keepguard.ms_auth.application.service.exception.RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimitExceededException(
            com.keepguard.ms_auth.application.service.exception.RateLimitExceededException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String correlationId = resolveCorrelationId(request);

        log.warn("Rate limit excedido: message={}, path={}, correlationId={}",
                ex.getMessage(), path, correlationId);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/rate-limit-exceeded"));
        problemDetail.setTitle("Muitas solicitações");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", path);
        problemDetail.setProperty("errorCode", "TOO_MANY_REQUESTS");
        problemDetail.setProperty("correlationId", correlationId);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(problemDetail);
    }

    @ExceptionHandler(com.keepguard.ms_auth.application.service.exception.ResetTokenCooldownException.class)
    public ResponseEntity<ProblemDetail> handleResetTokenCooldownException(
            com.keepguard.ms_auth.application.service.exception.ResetTokenCooldownException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String clientId = request.getHeader("X-Client-ID");
        String correlationId = resolveCorrelationId(request);
        
        log.warn("Cooldown de reset de token ativo: message={}, path={}, correlationId={}, clientId={}", 
            ex.getMessage(), path, correlationId, clientId);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problemDetail.setType(URI.create("https://keepguard.com/problems/rate-limit-exceeded"));
        problemDetail.setTitle("Muitas solicitações");
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("path", path);
        problemDetail.setProperty("errorCode", "TOO_MANY_REQUESTS");
        problemDetail.setProperty("correlationId", correlationId);
        problemDetail.setProperty("userAgent", clientId);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(problemDetail);
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
        problemDetail.setProperty("correlationId", resolveCorrelationId(request));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}