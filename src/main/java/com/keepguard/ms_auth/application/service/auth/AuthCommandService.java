package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.ms_auth.domain.dto.auth.AuthLoginCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthRefreshTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthLogoutCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthValidateTokenQueryDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthChangePasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthResetPasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthRegisterLoginCommandDTO;
import com.keepguard.ms_auth.application.service.exception.EmailNotVerifiedException;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.PasswordHistoryRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.domain.entity.user.PasswordHistory;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.adapters.out.feign.UserClient;
import com.keepguard.lib_common.exception.InvalidPasswordException;
import com.keepguard.lib_common.logging.annotation.LogOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthCommandService {

    private final UserRepositoryPort userRepository;
    private final JwtService jwtService;
    private final TokenCachePort tokenCachePort;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepositoryPort passwordHistoryRepository;
    private final MetricsPort metricsPort;
    private final UserRoleRepositoryPort userRoleRepository;
    private final RoleRepositoryPort roleRepository;
    private final UserClient userClient;

    @Value("${cache.redis.ttl.reset-token:900}")
    private long resetTokenTtlSeconds;

    @LogOperation(
        operation = "USER_LOGIN",
        description = "Realizando login do usuário: {username}",
        audit = true,
        auditAction = "LOGIN",
        auditEntityType = "USER"
    )
    @Transactional
    public String login(AuthLoginCommandDTO request) {
        log.info("Processing login for username: {}", request.getUsername());

        User user = userRepository.findByUsernameAndXApplication(request.getUsername(), request.getXApplicationUuid())
                .orElseThrow(() -> {
                    log.warn("Login failed - User not found: username={}, application={}", request.getUsername(), request.getXApplicationUuid());
                    return new InvalidCredentialsException("User not found", "USER_NOT_FOUND", 
                        Map.of("username",  request.getUsername() != null ?  request.getUsername() : "null", "application", request.getXApplicationUuid().toString()));
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed - Invalid password: username={}, userId={}, application={}", 
                request.getUsername(), user.getCodeUser(), request.getXApplicationUuid());
            throw new InvalidCredentialsException("Invalid password", "INVALID_PASSWORD", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(), "application", request.getXApplicationUuid().toString()));
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login failed - User not active: username={}, userId={}, status={}, application={}",
                    request.getUsername(), user.getCodeUser(), user.getStatus(), request.getXApplicationUuid());
            throw new InvalidCredentialsException("User is not active", "USER_NOT_ACTIVE", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(),
                       "status", user.getStatus().toString(), "application", request.getXApplicationUuid().toString()));
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            log.warn("Login failed - Email not verified: username={}, userId={}, application={}",
                    request.getUsername(), user.getCodeUser(), request.getXApplicationUuid());
            throw new EmailNotVerifiedException("Email not verified", "EMAIL_NOT_VERIFIED", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(), "application", request.getXApplicationUuid().toString()));
        }

        // Buscar roles e authorities do usuário
        List<String> roleNames = getUserRoles(user.getId());
        List<String> authorities = getUserAuthorities(user.getId());

        // Buscar displayHandle do ms-user (com fallback gracioso)
        String displayHandle = getDisplayHandle(user.getCodeUser(), request.getXApplicationUuid().toString());

        // Gerar token
        String token = jwtService.generateToken(user, roleNames, authorities, request.getXApplicationUuid().toString(), request.getUserAgent(), displayHandle);

        // Atualizar último login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Salvar token
        tokenCachePort.saveToken(user.getCodeUser().toString(), token, jwtService.getExpiration());

        metricsPort.incrementCounter("auth_login_success_total",
            Map.of("application", request.getXApplicationUuid().toString()));

        return token;
    }

    @LogOperation(
        operation = "REGISTER_LOGIN",
        description = "Realizando login após registro com senha criptografada: {username}",
        audit = true,
        auditAction = "LOGIN",
        auditEntityType = "USER"
    )
    @Transactional
    public String registerLogin(AuthRegisterLoginCommandDTO request) {
        log.info("Processing register login for username: {}", request.getUsername());

        User user = userRepository.findByUsernameAndXApplication(request.getUsername(), request.getXApplicationUuid())
                .orElseThrow(() -> {
                    log.warn("Register login failed - User not found: username={}, application={}", request.getUsername(), request.getXApplicationUuid());
                    return new InvalidCredentialsException("User not found", "USER_NOT_FOUND", 
                        Map.of("username", request.getUsername() != null ? request.getUsername() : "null", "application", request.getXApplicationUuid().toString()));
                });

        // Compara hash com hash diretamente (sem usar passwordEncoder.matches)
        if (!request.getPasswordHash().equals(user.getPasswordHash())) {
            log.warn("Register login failed - Invalid password hash: username={}, userId={}, application={}", 
                request.getUsername(), user.getCodeUser(), request.getXApplicationUuid());
            throw new InvalidCredentialsException("Invalid password", "INVALID_PASSWORD", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(), "application", request.getXApplicationUuid().toString()));
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Register login failed - User not active: username={}, userId={}, status={}, application={}",
                    request.getUsername(), user.getCodeUser(), user.getStatus(), request.getXApplicationUuid());
            throw new InvalidCredentialsException("User is not active", "USER_NOT_ACTIVE", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(),
                       "status", user.getStatus().toString(), "application", request.getXApplicationUuid().toString()));
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            log.warn("Register login failed - Email not verified: username={}, userId={}, application={}",
                    request.getUsername(), user.getCodeUser(), request.getXApplicationUuid());
            throw new EmailNotVerifiedException("Email not verified", "EMAIL_NOT_VERIFIED", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(), "application", request.getXApplicationUuid().toString()));
        }

        // Buscar roles e authorities do usuário
        List<String> roleNames = getUserRoles(user.getId());
        List<String> authorities = getUserAuthorities(user.getId());

        // Buscar displayHandle do ms-user (com fallback gracioso)
        String displayHandle = getDisplayHandle(user.getCodeUser(), request.getXApplicationUuid().toString());

        // Gerar token
        String token = jwtService.generateToken(user, roleNames, authorities, request.getXApplicationUuid().toString(), request.getUserAgent(), displayHandle);

        // Atualizar último login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Salvar token
        tokenCachePort.saveToken(user.getCodeUser().toString(), token, jwtService.getExpiration());

        metricsPort.incrementCounter("auth_register_login_success_total",
            Map.of("application", request.getXApplicationUuid().toString()));

        log.info("Register login successful for user: {} with application: {}", request.getUsername(), request.getXApplicationUuid());
        return token;
    }

    @LogOperation(
        operation = "REFRESH_TOKEN",
        description = "Renovando token de acesso",
        audit = true,
        auditAction = "REFRESH_TOKEN",
        auditEntityType = "USER"
    )
    @Transactional
    public String refreshToken(AuthRefreshTokenCommandDTO request) {
        log.info("Processing refresh token request - application={}, userAgent={}", 
            request.getXApplicationUuid(), request.getUserAgent());

        if (!jwtService.validateToken(request.getToken())) {
            throw new InvalidCredentialsException("Invalid token", "INVALID_TOKEN", 
                Map.of("application", request.getXApplicationUuid().toString()));
        }

        UUID codeUser = jwtService.extractUserId(request.getToken());
        
        User user = userRepository.findByCodeUserAndXApplication(codeUser, request.getXApplicationUuid())
                .orElseThrow(() -> {
                    log.warn("Refresh token failed - User not found: codeUser={}, application={}", codeUser, request.getXApplicationUuid());
                    return new NotFoundException("User not found", "USER_NOT_FOUND", 
                        Map.of("codeUser", codeUser.toString(), "application", request.getXApplicationUuid().toString()));
                });

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("User is not active", "USER_NOT_ACTIVE", 
                Map.of("codeUser", codeUser.toString(), 
                "status", user.getStatus().toString(), 
                "application", request.getXApplicationUuid().toString()));
        }

        // Buscar roles e authorities do usuário
        List<String> roleNames = getUserRoles(user.getId());
        List<String> authorities = getUserAuthorities(user.getId());

        // Buscar displayHandle do ms-user (com fallback gracioso)
        String displayHandle = getDisplayHandle(user.getCodeUser(), request.getXApplicationUuid().toString());

        String newToken = jwtService.generateToken(user, roleNames, authorities, request.getXApplicationUuid().toString(), request.getUserAgent(), displayHandle);

        tokenCachePort.removeToken(codeUser.toString(), request.getToken());
        tokenCachePort.saveToken(user.getCodeUser().toString(), newToken, jwtService.getExpiration());

        return newToken;
    }

    @LogOperation(
        operation = "USER_LOGOUT",
        description = "Realizando logout do usuário",
        audit = true,
        auditAction = "LOGOUT",
        auditEntityType = "USER"
    )
    @Transactional
    public void logout(AuthLogoutCommandDTO request) {
        log.info("Processing logout request - application={}", request.getXApplicationUuid());

        UUID codeUser = jwtService.extractUserId(request.getToken());
        tokenCachePort.removeAllTokens(codeUser.toString());

        metricsPort.incrementCounter("auth_logouts_total",
            Map.of("codeUser", codeUser.toString(), "application", request.getXApplicationUuid().toString()));
    }

    @LogOperation(
        operation = "VALIDATE_TOKEN",
        description = "Validando token JWT",
        audit = true,
        auditAction = "VALIDATE_TOKEN",
        auditEntityType = "USER"
    )
    @Transactional(readOnly = true)
    public void validateToken(AuthValidateTokenQueryDTO request) {
        log.info("Processing token validation request - application={}", request.getXApplicationUuid());

        if (!jwtService.validateToken(request.getToken())) {
            throw new InvalidCredentialsException("Token inválido ou expirado", "INVALID_TOKEN", 
                Map.of("application", request.getXApplicationUuid().toString()));
        }

        UUID codeUser = jwtService.extractUserId(request.getToken());

        userRepository.findByCodeUserAndXApplication(codeUser, request.getXApplicationUuid())
                .orElseThrow(() -> {
                    log.warn("Refresh token failed - User not found: codeUser={}, application={}", codeUser, request.getXApplicationUuid());
                    return new NotFoundException("User not found", "USER_NOT_FOUND", 
                        Map.of("codeUser", codeUser.toString(), "application", request.getXApplicationUuid().toString()));
                });

        if (!tokenCachePort.isTokenValid(codeUser.toString(), request.getToken())) {
            throw new InvalidCredentialsException("Token inválido ou expirado", "INVALID_TOKEN", 
                Map.of("codeUser", codeUser.toString(), "application", request.getXApplicationUuid().toString()));
        }

        metricsPort.incrementCounter("auth_token_validations_total",
            Map.of("codeUser", codeUser.toString(), "application", request.getXApplicationUuid().toString()));
    }

    @LogOperation(
        operation = "CHANGE_PASSWORD",
        description = "Alterando senha do usuário: {request.codeUser}",
        audit = true,
        auditAction = "CHANGE_PASSWORD",
        auditEntityType = "USER"
    )
    @Transactional
    public void changePassword(AuthChangePasswordCommandDTO request) {
        log.info("Processing password change request - codeUser={}, application={}", 
            request.getCodeUser(), request.getXApplicationUuid());

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidPasswordException("New password and confirmation do not match");
        }
        
        User user = userRepository.findByCodeUserAndXApplication(UUID.fromString(request.getCodeUser()), request.getXApplicationUuid())
            .orElseThrow(() -> {
                log.warn("Change password failed - User not found: codeUser={}, application={}", request.getCodeUser(), request.getXApplicationUuid());
            return new NotFoundException("User not found", "USER_NOT_FOUND", 
            Map.of("codeUser", request.getCodeUser(), "application", request.getXApplicationUuid().toString()));
        });

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("User is not active");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        String newHash = passwordEncoder.encode(request.getNewPassword());
        List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId());
        for (PasswordHistory h : history) {
            if (passwordEncoder.matches(request.getNewPassword(), h.getPasswordHash())) {
                throw new InvalidPasswordException("New password cannot be the same as one of the last 5 passwords");
            }
        }

        String oldHash = user.getPasswordHash();
        user.setPasswordHash(newHash);
        userRepository.save(user);

        passwordHistoryRepository.save(PasswordHistory.builder()
                .userId(user.getId())
                .passwordHash(oldHash)
                .createdAt(LocalDateTime.now())
                .build());

        metricsPort.incrementCounter("user_password_changes_total",
            Map.of("codeUser", request.getCodeUser()));
    }

    @LogOperation(
        operation = "GENERATE_RESET_TOKEN",
        description = "Gerando token de reset para usuário: {request.codeUser}",
        audit = true,
        auditAction = "GENERATE_RESET_TOKEN",
        auditEntityType = "USER"
    )
    @Transactional(readOnly = true)
    public AuthGenerateResetTokenViewDTO generateResetToken(AuthGenerateResetTokenCommandDTO request) {
        log.info("Gerando token de reset | codeUser={} | application={}", 
            request.getCodeUser(), request.getXApplicationUuid());

        // Valida se o usuário existe e está ativo
        User user = userRepository.findByCodeUserAndXApplication(
            UUID.fromString(request.getCodeUser()), 
            request.getXApplicationUuid())
            .orElseThrow(() -> {
                log.warn("Geração de token falhou - Usuário não encontrado: codeUser={}, application={}", 
                    request.getCodeUser(), request.getXApplicationUuid());
                return new NotFoundException("Usuário não encontrado", "USER_NOT_FOUND", 
                    Map.of("codeUser", request.getCodeUser(), 
                           "application", request.getXApplicationUuid().toString()));
            });

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Geração de token falhou - Usuário não está ativo: codeUser={}, status={}", 
                request.getCodeUser(), user.getStatus());
            throw new InvalidCredentialsException("Usuário não está ativo", "USER_NOT_ACTIVE", 
                Map.of("codeUser", request.getCodeUser(), 
                       "status", user.getStatus().toString()));
        }

        // Gera e salva o token no cache (Redis) com chave composta
        String token = tokenCachePort.generateAndSaveResetToken(
            request.getCodeUser(),
            request.getMessageType().name(),
            request.getTemplateType().name()
        );

        log.info("Token de reset gerado com sucesso | codeUser={} | messageType={} | templateType={} | ttl={}s", 
            request.getCodeUser(), request.getMessageType(), request.getTemplateType(), resetTokenTtlSeconds);

        // Registra métrica
        metricsPort.incrementCounter("reset_tokens_generated_total", 
            Map.of("messageType", request.getMessageType().toString(),
                   "templateType", request.getTemplateType().toString()));

        return AuthGenerateResetTokenViewDTO.builder()
            .codeUser(request.getCodeUser())
            .messageType(request.getMessageType())
            .communicationType(request.getCommunicationType())
            .templateType(request.getTemplateType())
            .token(token)
            .expiresInSeconds(resetTokenTtlSeconds)
            .build();
    }

    @LogOperation(
        operation = "RESET_PASSWORD",
        description = "Redefinindo senha do usuário: {request.codeUser}",
        audit = true,
        auditAction = "RESET_PASSWORD",
        auditEntityType = "USER"
    )
    @Transactional
    public void resetPassword(AuthResetPasswordCommandDTO request) {
        log.info("Processing password reset request - codeUser={}, application={}, messageType={}, templateType={}, resetToken={}", 
            request.getCodeUser(), request.getXApplicationUuid(), 
            request.getMessageType(), request.getTemplateType(), request.getResetToken());

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidPasswordException("New password and confirmation do not match");
        }
        
        User user = userRepository.findByCodeUserAndXApplication(UUID.fromString(request.getCodeUser()), request.getXApplicationUuid())
            .orElseThrow(() -> {
                log.warn("Change password failed - User not found: codeUser={}, application={}", request.getCodeUser(), request.getXApplicationUuid());
            return new NotFoundException("User not found", "USER_NOT_FOUND", 
            Map.of("codeUser", request.getCodeUser(), "application", request.getXApplicationUuid().toString()));
        });

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("User is not active");
        }

        // Valida o token usando a chave composta
        if (!tokenCachePort.isResetTokenValid(
                request.getCodeUser(), 
                request.getMessageType().name(), 
                request.getTemplateType().name(), 
                request.getResetToken())) {
            log.warn("Token de reset inválido ou expirado | codeUser={} | messageType={} | templateType={}", 
                request.getCodeUser(), request.getMessageType(), request.getTemplateType());
            throw new InvalidCredentialsException("Invalid or expired reset token");
        }

        String newHash = passwordEncoder.encode(request.getNewPassword());
        List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId());
        for (PasswordHistory h : history) {
            if (passwordEncoder.matches(request.getNewPassword(), h.getPasswordHash())) {
                throw new InvalidPasswordException("New password cannot be the same as one of the last 5 passwords");
            }
        }

        String oldHash = user.getPasswordHash();
        user.setPasswordHash(newHash);
        userRepository.save(user);

        passwordHistoryRepository.save(PasswordHistory.builder()
                .userId(user.getId())
                .passwordHash(oldHash)
                .createdAt(LocalDateTime.now())
                .build());

        // Remove o token usando a chave composta
        tokenCachePort.removeResetToken(
            request.getCodeUser(),
            request.getMessageType().name(),
            request.getTemplateType().name()
        );

        log.info("Senha resetada com sucesso | codeUser={} | messageType={} | templateType={}", 
            request.getCodeUser(), request.getMessageType(), request.getTemplateType());
    }

    /**
     * Busca os nomes das roles do usuário
     * @param userId ID do usuário
     * @return Lista com os nomes das roles
     */
    private List<String> getUserRoles(UUID userId) {
        List<UUID> roleIds = userRoleRepository.findByUserId(userId)
            .stream()
            .map(userRole -> userRole.getRoleId())
            .toList();

        return roleIds.stream()
            .map(roleId -> roleRepository.findById(roleId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Role::getName)
            .collect(Collectors.toList());
    }

    /**
     * Busca todas as authorities do usuário (sem repetição)
     * Soma todas as authorities de todas as roles associadas ao usuário
     * @param userId ID do usuário
     * @return Lista com todas as authorities únicas, ordenadas alfabeticamente
     */
    private List<String> getUserAuthorities(UUID userId) {
        List<UUID> roleIds = userRoleRepository.findByUserId(userId)
            .stream()
            .map(userRole -> userRole.getRoleId())
            .toList();

        // Busca todas as authorities de todas as roles e remove duplicatas
        return roleIds.stream()
            .map(roleId -> roleRepository.findById(roleId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .flatMap(role -> role.getAuthorities().stream())
            .map(authority -> authority.getName())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Busca displayHandle do ms-user com fallback gracioso
     * Se não conseguir buscar ou se displayHandle não estiver disponível, retorna null
     * 
     * @param codeUser Código único do usuário
     * @param xApplication UUID da aplicação
     * @return displayHandle ou null se não disponível
     */
    private String getDisplayHandle(UUID codeUser, String xApplication) {
        try {
            Map<String, Object> userData = userClient.getUserByCode(codeUser, xApplication);
            if (userData != null) {
                // display_handle está na raiz do user (ms-user)
                String displayHandle = (String) userData.get("display_handle");
                if (displayHandle != null && !displayHandle.trim().isEmpty()) {
                    log.debug("displayHandle encontrado para codeUser: {} - {}", codeUser, displayHandle);
                    return displayHandle;
                }
            }
        } catch (Exception e) {
            // Fallback gracioso: se não conseguir buscar, continua sem displayHandle
            log.debug("Não foi possível buscar displayHandle do ms-user para codeUser: {} - {}", codeUser, e.getMessage());
        }
        return null;
    }
}