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
import com.keepguard.ms_auth.application.port.out.persistence.DeviceBlacklistRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserDeviceRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.domain.entity.user.PasswordHistory;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.adapters.out.feign.UserClient;
import com.keepguard.ms_auth.infrastructure.config.security.LoginAttemptService;
import com.keepguard.lib_common.exception.InvalidPasswordException;
import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_auth.adapters.out.feign.CompanyClient;
import com.keepguard.ms_auth.application.dto.auth.AvailableMfaChannelDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.port.out.cache.SessionCachePort;
import com.keepguard.ms_auth.domain.entity.session.DeviceChallengeSession;
import com.keepguard.ms_auth.domain.entity.session.UserSession;
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
    private final UserDeviceRepositoryPort userDeviceRepository;
    private final DeviceBlacklistRepositoryPort deviceBlacklistRepository;
    private final JwtService jwtService;
    private final TokenCachePort tokenCachePort;
    private final SessionCachePort sessionCachePort;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepositoryPort passwordHistoryRepository;
    private final MetricsPort metricsPort;
    private final UserRoleRepositoryPort userRoleRepository;
    private final RoleRepositoryPort roleRepository;
    private final UserClient userClient;
    private final CompanyClient companyClient;
    private final LoginAttemptService loginAttemptService;

    @Value("${cache.redis.ttl.reset-token}")
    private long resetTokenTtlSeconds;

    @LogOperation(
        operation = "LOGIN",
        description = "Realizando login para usuário: {username}",
        audit = true,
        auditAction = "LOGIN",
        auditEntityType = "USER"
    )
    @Transactional
    public AuthLoginView login(AuthLoginCommandDTO request) {
        log.info("Processing login for username: {}", request.getUsername());

        // 1. Verificar se a conta está bloqueada por excesso de tentativas
        if (loginAttemptService.isAccountLocked(request.getUsername())) {
            long remainingTime = loginAttemptService.getRemainingLockoutTime(request.getUsername());
            log.warn("Tentativa de login para conta bloqueada: username={}, remainingTime={} min", 
                    request.getUsername(), remainingTime);
            throw new com.keepguard.ms_auth.application.service.exception.AccountLockedException(
                    "Conta temporariamente bloqueada. Tente novamente em " + remainingTime + " minutos.");
        }

        User user = userRepository.findByUsernameAndTenantId(request.getUsername(), request.getTenantId())
                .orElseThrow(() -> {
                    log.warn("Login failed - User not found: username={}, application={}", request.getUsername(), request.getTenantId());
                    loginAttemptService.recordFailedAttempt(request.getUsername());
                    return new InvalidCredentialsException("User not found", "USER_NOT_FOUND", 
                        Map.of("username",  request.getUsername() != null ?  request.getUsername() : "null", "application", request.getTenantId().toString()));
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed - Invalid password: username={}, userId={}, application={}", 
                request.getUsername(), user.getCodeUser(), request.getTenantId());
            loginAttemptService.recordFailedAttempt(request.getUsername());
            throw new InvalidCredentialsException("Invalid password", "INVALID_PASSWORD", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(), "application", request.getTenantId().toString()));
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login failed - User not active: username={}, userId={}, status={}, application={}",
                    request.getUsername(), user.getCodeUser(), user.getStatus(), request.getTenantId());
            throw new InvalidCredentialsException("User is not active", "USER_NOT_ACTIVE", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(),
                       "status", user.getStatus().toString(), "application", request.getTenantId().toString()));
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            log.warn("Login failed - Email not verified: username={}, userId={}, application={}",
                    request.getUsername(), user.getCodeUser(), request.getTenantId());
            throw new EmailNotVerifiedException("Email not verified", "EMAIL_NOT_VERIFIED", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(), "application", request.getTenantId().toString()));
        }

        // Login bem-sucedido: limpa contador de tentativas e bloqueios
        loginAttemptService.recordSuccessfulAttempt(request.getUsername());

        // 2. Identificar e checar se o dispositivo é confiável
        String deviceId = (request.getDeviceId() != null && !request.getDeviceId().isBlank())
                ? request.getDeviceId()
                : "dev_default_" + user.getCodeUser().toString().substring(0, 8);

        // Verificar se o dispositivo está na Blacklist do usuário (Redis com fallback no PostgreSQL)
        boolean isBlacklisted = sessionCachePort.isDeviceBlacklisted(user.getCodeUser().toString(), deviceId)
                || deviceBlacklistRepository.isBlacklisted(user.getCodeUser(), deviceId);

        if (isBlacklisted) {
            log.warn("Tentativa de login rejeitada - Dispositivo bloqueado na blacklist: codeUser={}, deviceId={}",
                    user.getCodeUser(), deviceId);
            throw new com.keepguard.ms_auth.application.service.exception.DeviceBlacklistedException(
                    "Este dispositivo foi bloqueado para acesso a esta conta.",
                    "DEVICE_BLACKLISTED",
                    Map.of("codeUser", user.getCodeUser().toString(), "deviceId", deviceId)
            );
        }

        Optional<UserSession> existingSession = sessionCachePort.getUserSession(user.getCodeUser().toString(), deviceId);
        boolean isDeviceTrusted = existingSession.map(UserSession::getIsTrusted).orElse(false);

        // Se o dispositivo NÃO for confiável, inicia o desafio MFA (Step-Up)
        if (!isDeviceTrusted) {
            log.info("Dispositivo não confiável detectado | codeUser={} | deviceId={}. Iniciando desafio MFA.",
                    user.getCodeUser(), deviceId);

            List<AvailableMfaChannelDTO> availableChannels = fetchAvailableChannels(request.getTenantId().toString(), user);
            
            // Se houver canais de MFA ativos para a empresa, retorna o desafio
            if (!availableChannels.isEmpty()) {
                String challengeSessionId = "chal_" + UUID.randomUUID();
                String phone = getUserPhone(user.getCodeUser(), request.getTenantId().toString());
                
                DeviceChallengeSession challenge = DeviceChallengeSession.builder()
                        .challengeSessionId(challengeSessionId)
                        .codeUser(user.getCodeUser().toString())
                        .tenantId(request.getTenantId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(phone)
                        .clientId(request.getClientId())
                        .deviceId(deviceId)
                        .deviceName(request.getDeviceName() != null ? request.getDeviceName() : "Navegador Web")
                        .deviceType(request.getDeviceType() != null ? request.getDeviceType() : "DESKTOP")
                        .ipAddress(request.getIpAddress())
                        .userAgent(request.getUserAgent())
                        .attempts(0)
                        .maxAttempts(5)
                        .expiresAt(System.currentTimeMillis() + (600 * 1000))
                        .build();

                sessionCachePort.saveDeviceChallenge(challenge, 600);

                return new AuthLoginView(
                        null,
                        null,
                        "MFA_REQUIRED",
                        challengeSessionId,
                        false,
                        availableChannels
                );
            }
        }

        // Dispositivo confiável: emite o token JWT final
        List<String> roleNames = getUserRoles(user.getId());
        List<String> authorities = getUserAuthorities(user.getId());
        String displayHandle = getDisplayHandle(user.getCodeUser(), request.getTenantId().toString());

        String token = jwtService.generateToken(user, roleNames, authorities, request.getTenantId().toString(), request.getClientId(), displayHandle, deviceId);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Salva token legado para compatibilidade
        tokenCachePort.saveToken(user.getCodeUser().toString(), token, jwtService.getExpiration());

        // Salva/Atualiza sessão por dispositivo no Redis (30 dias)
        UserSession session = UserSession.builder()
                .sessionId("sess_" + UUID.randomUUID())
                .codeUser(user.getCodeUser().toString())
                .tenantId(request.getTenantId().toString())
                .clientId(request.getClientId())
                .deviceId(deviceId)
                .deviceName(request.getDeviceName() != null ? request.getDeviceName() : "Navegador Web")
                .deviceType(request.getDeviceType() != null ? request.getDeviceType() : "DESKTOP")
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .isTrusted(true)
                .lastActiveAt(LocalDateTime.now().toString())
                .createdAt(existingSession.map(UserSession::getCreatedAt).orElse(LocalDateTime.now().toString()))
                .build();

        sessionCachePort.saveUserSession(session, 2592000L); // 30 dias

        // Atualiza atividade do dispositivo no PostgreSQL
        try {
            userDeviceRepository.findByCodeUserAndDeviceId(user.getCodeUser(), deviceId)
                    .ifPresentOrElse(dev -> {
                        dev.updateActivity(request.getIpAddress(), request.getUserAgent(), LocalDateTime.now());
                        userDeviceRepository.save(dev);
                    }, () -> {
                        com.keepguard.ms_auth.domain.entity.session.UserDevice newDevice = com.keepguard.ms_auth.domain.entity.session.UserDevice.builder()
                                .codeUser(user.getCodeUser())
                                .tenantId(request.getTenantId())
                                .deviceId(deviceId)
                                .deviceName(request.getDeviceName() != null ? request.getDeviceName() : "Navegador Web")
                                .deviceType(request.getDeviceType() != null ? request.getDeviceType() : "DESKTOP")
                                .ipAddress(request.getIpAddress())
                                .userAgent(request.getUserAgent())
                                .isTrusted(true)
                                .firstSeenAt(LocalDateTime.now())
                                .lastActiveAt(LocalDateTime.now())
                                .build();
                        userDeviceRepository.save(newDevice);
                    });
        } catch (Exception e) {
            log.warn("Falha ao registrar atividade do dispositivo no banco | codeUser={} | deviceId={} | erro={}",
                    user.getCodeUser(), deviceId, e.getMessage());
        }

        metricsPort.incrementCounter("auth_login_success_total",
            Map.of("application", request.getTenantId().toString()));

        return new AuthLoginView(token, 3600L, "AUTHENTICATED", null, true, null);
    }

    private List<AvailableMfaChannelDTO> fetchAvailableChannels(String tenantId, User user) {
        List<AvailableMfaChannelDTO> channels = new ArrayList<>();
        try {
            Map<String, Object> company = companyClient.getCompanyByTenantId(tenantId);
            String userPhone = getUserPhone(user.getCodeUser(), tenantId);
            if (company != null && company.containsKey("mfaChannels")) {
                List<Map<String, Object>> mfaList = (List<Map<String, Object>>) company.get("mfaChannels");
                for (Map<String, Object> ch : mfaList) {
                    Boolean enabled = (Boolean) ch.get("enabled");
                    String channelName = (String) ch.get("channel");

                    if (Boolean.TRUE.equals(enabled) && channelName != null) {
                        String upper = channelName.toUpperCase();
                        if ("EMAIL".equals(upper)) {
                            channels.add(new AvailableMfaChannelDTO("EMAIL", maskEmail(user.getEmail()), "Receber código por E-mail"));
                        } else if ("SMS".equals(upper)) {
                            channels.add(new AvailableMfaChannelDTO("SMS", maskPhone(userPhone), "Receber código por SMS"));
                        } else if ("WHATSAPP".equals(upper)) {
                            channels.add(new AvailableMfaChannelDTO("WHATSAPP", maskPhone(userPhone), "Receber código pelo WhatsApp"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível carregar políticas de MFA da Company | tenantId={} | fallback padrão: EMAIL", tenantId);
            channels.add(new AvailableMfaChannelDTO("EMAIL", maskEmail(user.getEmail()), "Receber código por E-mail"));
        }
        return channels;
    }

    private String getUserPhone(UUID codeUser, String tenantId) {
        try {
            Map<String, Object> userData = userClient.getUserByCode(codeUser, tenantId);
            if (userData != null) {
                if (userData.containsKey("phoneE164") && userData.get("phoneE164") != null) {
                    return (String) userData.get("phoneE164");
                }
                if (userData.containsKey("phone") && userData.get("phone") != null) {
                    return (String) userData.get("phone");
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível buscar telefone do usuário | codeUser={} | erro={}", codeUser, e.getMessage());
        }
        return null;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "e***@***.com";
        String[] parts = email.split("@");
        String name = parts[0];
        String masked = name.length() > 2 ? name.substring(0, 1) + "***" + name.substring(name.length() - 1) : name + "***";
        return masked + "@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) return "+55 ** *****-****";
        return phone.substring(0, 4) + " *****-" + phone.substring(phone.length() - 4);
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

        User user = userRepository.findByUsernameAndTenantId(request.getUsername(), request.getTenantId())
                .orElseThrow(() -> {
                    log.warn("Register login failed - User not found: username={}, application={}", request.getUsername(), request.getTenantId());
                    return new InvalidCredentialsException("User not found", "USER_NOT_FOUND", 
                        Map.of("username", request.getUsername() != null ? request.getUsername() : "null", "application", request.getTenantId().toString()));
                });

        // Compara hash com hash diretamente (sem usar passwordEncoder.matches)
        if (!request.getPasswordHash().equals(user.getPasswordHash())) {
            log.warn("Register login failed - Invalid password hash: username={}, userId={}, application={}", 
                request.getUsername(), user.getCodeUser(), request.getTenantId());
            throw new InvalidCredentialsException("Invalid password", "INVALID_PASSWORD", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(), "application", request.getTenantId().toString()));
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Register login failed - User not active: username={}, userId={}, status={}, application={}",
                    request.getUsername(), user.getCodeUser(), user.getStatus(), request.getTenantId());
            throw new InvalidCredentialsException("User is not active", "USER_NOT_ACTIVE", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(),
                       "status", user.getStatus().toString(), "application", request.getTenantId().toString()));
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            log.warn("Register login failed - Email not verified: username={}, userId={}, application={}",
                    request.getUsername(), user.getCodeUser(), request.getTenantId());
            throw new EmailNotVerifiedException("Email not verified", "EMAIL_NOT_VERIFIED", 
                Map.of("username", request.getUsername(), "userId", user.getCodeUser().toString(), "application", request.getTenantId().toString()));
        }

        // Buscar roles e authorities do usuário
        List<String> roleNames = getUserRoles(user.getId());
        List<String> authorities = getUserAuthorities(user.getId());

        // Buscar displayHandle do ms-user (com fallback gracioso)
        String displayHandle = getDisplayHandle(user.getCodeUser(), request.getTenantId().toString());

        // Gerar token
        String token = jwtService.generateToken(user, roleNames, authorities, request.getTenantId().toString(), request.getClientId(), displayHandle);

        // Atualizar último login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Single session: invalida todas as sessões anteriores antes de criar a nova
        tokenCachePort.removeAllTokens(user.getCodeUser().toString());
        log.info("Sessões anteriores invalidadas | codeUser={}", user.getCodeUser());

        // Salvar novo token
        tokenCachePort.saveToken(user.getCodeUser().toString(), token, jwtService.getExpiration());

        metricsPort.incrementCounter("auth_register_login_success_total",
            Map.of("application", request.getTenantId().toString()));

        log.info("Register login successful for user: {} with application: {}", request.getUsername(), request.getTenantId());
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
        log.info("Processing refresh token request - application={}, clientId={}", 
            request.getTenantId(), request.getClientId());

        if (!jwtService.validateToken(request.getToken())) {
            throw new InvalidCredentialsException("Invalid token", "INVALID_TOKEN", 
                Map.of("application", request.getTenantId().toString()));
        }

        UUID codeUser = jwtService.extractUserId(request.getToken());

        // Valida se o token ainda está ativo no Redis (Refresh Token Rotation - RTR)
        if (!tokenCachePort.isTokenValid(codeUser.toString(), request.getToken())) {
            log.warn("Refresh token failed - Token revogado ou já rotacionado: codeUser={}, application={}", codeUser, request.getTenantId());
            throw new InvalidCredentialsException("Token revogado ou sessão encerrada", "TOKEN_REVOKED", 
                Map.of("codeUser", codeUser.toString(), "application", request.getTenantId().toString()));
        }
        
        User user = userRepository.findByCodeUserAndTenantId(codeUser, request.getTenantId())
                .orElseThrow(() -> {
                    log.warn("Refresh token failed - User not found: codeUser={}, application={}", codeUser, request.getTenantId());
                    return new NotFoundException("User not found", "USER_NOT_FOUND", 
                        Map.of("codeUser", codeUser.toString(), "application", request.getTenantId().toString()));
                });

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("User is not active", "USER_NOT_ACTIVE", 
                Map.of("codeUser", codeUser.toString(), 
                "status", user.getStatus().toString(), 
                "application", request.getTenantId().toString()));
        }

        // Buscar roles e authorities do usuário
        List<String> roleNames = getUserRoles(user.getId());
        List<String> authorities = getUserAuthorities(user.getId());

        // Buscar displayHandle do ms-user (com fallback gracioso)
        String displayHandle = getDisplayHandle(user.getCodeUser(), request.getTenantId().toString());

        String newToken = jwtService.generateToken(user, roleNames, authorities, request.getTenantId().toString(), request.getClientId(), displayHandle);

        // Remove o token antigo e salva o novo (rotação de token)
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
        log.info("Processing logout request - application={}", request.getTenantId());

        if (!jwtService.validateToken(request.getToken())) {
            log.warn("Logout failed - Token com assinatura inválida: application={}", request.getTenantId());
            throw new InvalidCredentialsException("Token inválido", "INVALID_TOKEN",
                Map.of("application", request.getTenantId().toString()));
        }

        UUID codeUser = jwtService.extractUserId(request.getToken());

        // Garante que o token ainda está ativo no Redis antes de encerrar a sessão
        if (!tokenCachePort.isTokenValid(codeUser.toString(), request.getToken())) {
            log.warn("Logout failed - Sessão já encerrada ou token revogado: codeUser={}, application={}", codeUser, request.getTenantId());
            throw new InvalidCredentialsException("Sessão já encerrada", "SESSION_ALREADY_TERMINATED",
                Map.of("codeUser", codeUser.toString(), "application", request.getTenantId().toString()));
        }

        tokenCachePort.removeAllTokens(codeUser.toString());

        metricsPort.incrementCounter("auth_logouts_total",
            Map.of("codeUser", codeUser.toString(), "application", request.getTenantId().toString()));
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
        log.info("Processing token validation request - application={}", request.getTenantId());

        if (!jwtService.validateToken(request.getToken())) {
            throw new InvalidCredentialsException("Token inválido ou expirado", "INVALID_TOKEN", 
                Map.of("application", request.getTenantId().toString()));
        }

        UUID codeUser = jwtService.extractUserId(request.getToken());

        userRepository.findByCodeUserAndTenantId(codeUser, request.getTenantId())
                .orElseThrow(() -> {
                    log.warn("Refresh token failed - User not found: codeUser={}, application={}", codeUser, request.getTenantId());
                    return new NotFoundException("User not found", "USER_NOT_FOUND", 
                        Map.of("codeUser", codeUser.toString(), "application", request.getTenantId().toString()));
                });

        if (!tokenCachePort.isTokenValid(codeUser.toString(), request.getToken())) {
            throw new InvalidCredentialsException("Token inválido ou expirado", "INVALID_TOKEN", 
                Map.of("codeUser", codeUser.toString(), "application", request.getTenantId().toString()));
        }

        metricsPort.incrementCounter("auth_token_validations_total",
            Map.of("codeUser", codeUser.toString(), "application", request.getTenantId().toString()));
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
            request.getCodeUser(), request.getTenantId());

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidPasswordException("New password and confirmation do not match");
        }
        
        User user = userRepository.findByCodeUserAndTenantId(UUID.fromString(request.getCodeUser()), request.getTenantId())
            .orElseThrow(() -> {
                log.warn("Change password failed - User not found: codeUser={}, application={}", request.getCodeUser(), request.getTenantId());
            return new NotFoundException("User not found", "USER_NOT_FOUND", 
            Map.of("codeUser", request.getCodeUser(), "application", request.getTenantId().toString()));
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
            request.getCodeUser(), request.getTenantId());

        // 1. Verificar se o cooldown para nova geração está ativo
        if (tokenCachePort.isResetTokenCooldownActive(request.getCodeUser())) {
            long remainingCooldown = tokenCachePort.getResetTokenCooldownRemaining(request.getCodeUser());
            log.warn("Geração de token de reset bloqueada por cooldown | codeUser={} | remainingCooldown={}s", 
                request.getCodeUser(), remainingCooldown);
            throw new com.keepguard.ms_auth.application.service.exception.ResetTokenCooldownException(
                "Aguarde " + remainingCooldown + " segundos antes de solicitar um novo código.");
        }

        // Valida se o usuário existe e está ativo
        User user = userRepository.findByCodeUserAndTenantId(
            UUID.fromString(request.getCodeUser()), 
            request.getTenantId())
            .orElseThrow(() -> {
                log.warn("Geração de token falhou - Usuário não encontrado: codeUser={}, application={}", 
                    request.getCodeUser(), request.getTenantId());
                return new NotFoundException("Usuário não encontrado", "USER_NOT_FOUND", 
                    Map.of("codeUser", request.getCodeUser(), 
                           "application", request.getTenantId().toString()));
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
            request.getCodeUser(), request.getTenantId(), 
            request.getMessageType(), request.getTemplateType(), request.getResetToken());

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidPasswordException("New password and confirmation do not match");
        }
        
        User user = userRepository.findByCodeUserAndTenantId(UUID.fromString(request.getCodeUser()), request.getTenantId())
            .orElseThrow(() -> {
                log.warn("Change password failed - User not found: codeUser={}, application={}", request.getCodeUser(), request.getTenantId());
            return new NotFoundException("User not found", "USER_NOT_FOUND", 
            Map.of("codeUser", request.getCodeUser(), "application", request.getTenantId().toString()));
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
            
            // Registra tentativa falha e invalida se estourou o limite
            long attempts = tokenCachePort.recordResetTokenFailedAttempt(
                request.getCodeUser(),
                request.getMessageType().name(),
                request.getTemplateType().name()
            );

            log.warn("Token de reset inválido ou expirado | codeUser={} | messageType={} | templateType={} | tentativa={}", 
                request.getCodeUser(), request.getMessageType(), request.getTemplateType(), attempts);
            
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

        // Remove o token e limpa contadores usando a chave composta
        tokenCachePort.removeResetToken(
            request.getCodeUser(),
            request.getMessageType().name(),
            request.getTemplateType().name()
        );
        tokenCachePort.clearResetTokenAttempts(
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
     * @param tenantId UUID da aplicação
     * @return displayHandle ou null se não disponível
     */
    private String getDisplayHandle(UUID codeUser, String tenantId) {
        try {
            Map<String, Object> userData = userClient.getUserByCode(codeUser, tenantId);
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