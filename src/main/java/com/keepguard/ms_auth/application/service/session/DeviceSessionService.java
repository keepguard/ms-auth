package com.keepguard.ms_auth.application.service.session;

import com.keepguard.lib_common.utils.CodeGeneratorUtils;
import com.keepguard.ms_auth.adapters.out.feign.CommunicationClient;
import com.keepguard.ms_auth.adapters.out.feign.CompanyClient;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.session.DeviceSessionView;
import com.keepguard.ms_auth.application.dto.session.PasswordChangedNotifyCommand;
import com.keepguard.ms_auth.application.dto.session.SendDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.dto.session.VerifyDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.port.out.cache.SessionCachePort;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import com.keepguard.ms_auth.application.port.out.geo.GeoLocationPort;
import com.keepguard.ms_auth.application.port.out.persistence.DeviceBlacklistRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserDeviceRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.session.DeviceChallengeSession;
import com.keepguard.ms_auth.domain.entity.session.UserDevice;
import com.keepguard.ms_auth.domain.entity.session.UserSession;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import com.keepguard.ms_auth.infrastructure.util.ClientLocation;
import com.keepguard.ms_auth.infrastructure.util.IpAddressUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceSessionService {

    private final SessionCachePort sessionCachePort;
    private final TokenCachePort tokenCachePort;
    private final CommunicationClient communicationClient;
    private final UserRepositoryPort userRepository;
    private final UserRoleRepositoryPort userRoleRepository;
    private final RoleRepositoryPort roleRepository;
    private final UserDeviceRepositoryPort userDeviceRepository;
    private final DeviceBlacklistRepositoryPort deviceBlacklistRepository;
    private final CompanyClient companyClient;
    private final JwtService jwtService;
    private final GeoLocationPort geoLocationPort;

    @Value("${app.urls.frontend-base-url:http://localhost:3000}")
    private String defaultFrontendBaseUrl;

    @Value("${app.urls.api-base-url:http://localhost:8381}")
    private String defaultApiBaseUrl;

    public Map<String, Object> sendChallenge(SendDeviceChallengeCommandDTO command) {
        log.info("Disparando código de desafio para dispositivo | challengeSessionId={} | channel={}",
                command.getChallengeSessionId(), command.getChannel());

        DeviceChallengeSession challenge = sessionCachePort.getDeviceChallenge(command.getChallengeSessionId())
                .orElseThrow(() -> new NotFoundException("Sessão de desafio expirada ou inválida", "CHALLENGE_NOT_FOUND",
                        Map.of("challengeSessionId", command.getChallengeSessionId())));

        String channel = command.getChannel().toUpperCase();
        String code = CodeGeneratorUtils.generateSixDigitCode();
        challenge.setActiveCode(code);
        challenge.setSelectedChannel(channel);

        sessionCachePort.saveDeviceChallenge(challenge, 600); // 10 min

        // Montar variáveis e payload de envio para o ms-communication
        Map<String, Object> variables = new HashMap<>();
        variables.put("token", code);
        variables.put("expiresIn", 5);
        variables.put("deviceName", challenge.getDeviceName());
        variables.put("userName", challenge.getUsername());

        String templateType;
        String recipient;
        String messageType;

        if ("SMS".equals(channel)) {
            templateType = "AUTENTICACAO_DISPOSITIVO_SMS_TOKEN";
            recipient = challenge.getPhone();
            messageType = "SMS";
        } else if ("WHATSAPP".equals(channel)) {
            templateType = "AUTENTICACAO_DISPOSITIVO_WHATSAPP_TOKEN";
            recipient = challenge.getPhone();
            messageType = "WHATSAPP";
        } else {
            templateType = "AUTENTICACAO_DISPOSITIVO_EMAIL_TOKEN";
            recipient = challenge.getEmail();
            messageType = "EMAIL";
        }

        try {
            Map<String, Object> msgPayload = Map.of(
                    "recipient", recipient != null ? recipient : (challenge.getEmail() != null ? challenge.getEmail() : ""),
                    "codeUser", challenge.getCodeUser() != null ? challenge.getCodeUser() : "",
                    "templateType", templateType,
                    "messageType", messageType,
                    "communicationType", messageType,
                    "subject", "Código de Verificação de Dispositivo",
                    "variables", variables
            );
            communicationClient.sendMessage(msgPayload, challenge.getTenantId());
            log.info("Mensagem de desafio enviada com sucesso para ms-communication | templateType={}", templateType);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem de desafio via ms-communication: {}", e.getMessage(), e);
        }

        return Map.of(
                "message", "Código de verificação enviado para o canal " + channel,
                "channel", channel,
                "expiresIn", 300,
                "resendCooldown", 60
        );
    }

    public AuthLoginView verifyChallenge(VerifyDeviceChallengeCommandDTO command) {
        log.info("Validando código de desafio para dispositivo | challengeSessionId={}", command.getChallengeSessionId());

        DeviceChallengeSession challenge = sessionCachePort.getDeviceChallenge(command.getChallengeSessionId())
                .orElseThrow(() -> new NotFoundException("Sessão de desafio expirada ou inválida", "CHALLENGE_NOT_FOUND",
                        Map.of("challengeSessionId", command.getChallengeSessionId())));

        if (challenge.getActiveCode() == null || !challenge.getActiveCode().equals(command.getCode().trim())) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            if (challenge.getAttempts() >= challenge.getMaxAttempts()) {
                sessionCachePort.removeDeviceChallenge(command.getChallengeSessionId());
                throw new InvalidCredentialsException("Número máximo de tentativas excedido. Tente fazer login novamente.", "MAX_ATTEMPTS_EXCEEDED", Map.of());
            }
            sessionCachePort.saveDeviceChallenge(challenge, 600);
            throw new InvalidCredentialsException("Código de segurança inválido", "INVALID_CHALLENGE_CODE",
                    Map.of("attemptsRemaining", (challenge.getMaxAttempts() - challenge.getAttempts())));
        }

        // Código correto: buscar usuário
        User user = userRepository.findByCodeUserAndTenantId(
                UUID.fromString(challenge.getCodeUser()),
                UUID.fromString(challenge.getTenantId())
        ).orElseThrow(() -> new NotFoundException("Usuário não encontrado", "USER_NOT_FOUND", Map.of()));

        List<String> roleNames = getUserRoles(user.getId());
        List<String> authorities = getUserAuthorities(user.getId());

        String token = jwtService.generateToken(user, roleNames, authorities, challenge.getTenantId(), challenge.getClientId(), challenge.getDeviceId());

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Salva token legado
        tokenCachePort.saveToken(user.getCodeUser().toString(), token, jwtService.getExpiration());

        // Registra a sessão do dispositivo no Redis
        String ipAddress = IpAddressUtils.firstIp(challenge.getIpAddress());
        String location = geoLocationPort.resolve(ipAddress);

        UserSession session = UserSession.builder()
                .sessionId("sess_" + UUID.randomUUID())
                .codeUser(challenge.getCodeUser())
                .tenantId(challenge.getTenantId())
                .clientId(challenge.getClientId())
                .deviceId(challenge.getDeviceId())
                .deviceName(challenge.getDeviceName())
                .deviceType(challenge.getDeviceType())
                .ipAddress(ipAddress)
                .location(location)
                .userAgent(challenge.getUserAgent())
                .isTrusted(Boolean.TRUE.equals(command.getTrustDevice()))
                .lastActiveAt(LocalDateTime.now().toString())
                .createdAt(LocalDateTime.now().toString())
                .refreshToken(token)
                .build();

        sessionCachePort.saveUserSession(session, 2592000L); // 30 dias

        // Registra/Atualiza o dispositivo no PostgreSQL
        try {
            UUID codeUserUuid = UUID.fromString(challenge.getCodeUser());
            UUID tenantIdUuid = UUID.fromString(challenge.getTenantId());
            userDeviceRepository.findByCodeUserAndDeviceId(codeUserUuid, challenge.getDeviceId())
                    .ifPresentOrElse(dev -> {
                        dev.setIsTrusted(Boolean.TRUE.equals(command.getTrustDevice()));
                        dev.updateActivity(ipAddress, challenge.getUserAgent(), LocalDateTime.now());
                        userDeviceRepository.save(dev);
                    }, () -> {
                        UserDevice newDevice = UserDevice.builder()
                                .codeUser(codeUserUuid)
                                .tenantId(tenantIdUuid)
                                .deviceId(challenge.getDeviceId())
                                .deviceName(challenge.getDeviceName())
                                .deviceType(challenge.getDeviceType())
                                .ipAddress(ipAddress)
                                .userAgent(challenge.getUserAgent())
                                .isTrusted(Boolean.TRUE.equals(command.getTrustDevice()))
                                .firstSeenAt(LocalDateTime.now())
                                .lastActiveAt(LocalDateTime.now())
                                .build();
                        userDeviceRepository.save(newDevice);
                    });
        } catch (Exception e) {
            log.warn("Falha ao persistir dispositivo no PostgreSQL | codeUser={} | deviceId={} | erro={}",
                    challenge.getCodeUser(), challenge.getDeviceId(), e.getMessage());
        }

        // Gerar token de revogação rápida (válido por 48h = 172800s)
        String quickRevokeTokenStr = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken quickRevokeToken = com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken.builder()
                .token(quickRevokeTokenStr)
                .codeUser(challenge.getCodeUser())
                .tenantId(challenge.getTenantId())
                .deviceId(challenge.getDeviceId())
                .deviceName(challenge.getDeviceName())
                .ipAddress(challenge.getIpAddress())
                .userAgent(challenge.getUserAgent())
                .createdAt(LocalDateTime.now().toString())
                .expiresAt(System.currentTimeMillis() + (172800L * 1000L))
                .build();
        sessionCachePort.saveQuickRevokeToken(quickRevokeToken, 172800L);

        // Disparar e-mail de alerta de novo dispositivo autenticado com link de revogação rápida
        sendNewDeviceNotification(challenge, quickRevokeTokenStr);

        // Remove o challenge
        sessionCachePort.removeDeviceChallenge(command.getChallengeSessionId());

        log.info("Dispositivo verificado com sucesso! Sessão ativada | codeUser={} | deviceId={}", challenge.getCodeUser(), challenge.getDeviceId());

        return new AuthLoginView(token, 3600L, "AUTHENTICATED", null, true, null);
    }

    private void sendNewDeviceNotification(DeviceChallengeSession challenge, String quickRevokeToken) {
        if (challenge.getEmail() == null || challenge.getEmail().isBlank()) {
            return;
        }

        try {
            String baseUrl = resolveBaseUrl(challenge.getTenantId());
            String revokeUrl = baseUrl + "/api/v1/auth/device/quick-revoke?token=" + quickRevokeToken + "&blacklist=true";

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", challenge.getUsername() != null ? challenge.getUsername() : "");
            variables.put("deviceName", challenge.getDeviceName() != null ? challenge.getDeviceName() : "Dispositivo Desconhecido");
            variables.put("deviceType", challenge.getDeviceType() != null ? challenge.getDeviceType() : "DESKTOP");
            variables.put("ipAddress", challenge.getIpAddress() != null ? challenge.getIpAddress() : "IP não identificado");
            variables.put("userAgent", challenge.getUserAgent() != null ? challenge.getUserAgent() : "N/A");
            variables.put("loginTime", LocalDateTime.now().toString());
            variables.put("quickRevokeToken", quickRevokeToken);
            variables.put("revokeUrl", revokeUrl);

            Map<String, Object> msgPayload = Map.of(
                    "recipient", challenge.getEmail(),
                    "codeUser", challenge.getCodeUser() != null ? challenge.getCodeUser() : "",
                    "templateType", "NOVO_DISPOSITIVO_AUTENTICADO",
                    "messageType", "EMAIL",
                    "communicationType", "EMAIL",
                    "subject", "Alerta de Segurança: Novo dispositivo conectado à sua conta",
                    "variables", variables
            );

            communicationClient.sendMessage(msgPayload, challenge.getTenantId());
            log.info("Notificação de novo dispositivo conectado enviada | codeUser={} | email={} | revokeUrl={}",
                    challenge.getCodeUser(), challenge.getEmail(), revokeUrl);
        } catch (Exception e) {
            log.error("Falha ao enviar notificação de novo dispositivo conectado: {}", e.getMessage(), e);
        }
    }

    /**
     * Envia e-mail de alerta após troca/reset de senha, com link de quick-revoke
     * do dispositivo que executou a operação (quando deviceId estiver presente).
     */
    public void notifyPasswordChanged(PasswordChangedNotifyCommand command) {
        if (command == null || command.getEmail() == null || command.getEmail().isBlank()) {
            log.warn("Notificação de senha alterada ignorada - e-mail ausente | codeUser={}",
                    command != null ? command.getCodeUser() : null);
            return;
        }

        try {
            String quickRevokeTokenStr = null;
            String revokeUrl = "";

            if (command.getDeviceId() != null && !command.getDeviceId().isBlank()) {
                quickRevokeTokenStr = UUID.randomUUID().toString().replace("-", "")
                        + UUID.randomUUID().toString().replace("-", "");
                com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken quickRevokeToken =
                        com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken.builder()
                                .token(quickRevokeTokenStr)
                                .codeUser(command.getCodeUser())
                                .tenantId(command.getTenantId())
                                .deviceId(command.getDeviceId())
                                .deviceName(command.getDeviceName())
                                .ipAddress(command.getIpAddress())
                                .userAgent(command.getUserAgent())
                                .createdAt(LocalDateTime.now().toString())
                                .expiresAt(System.currentTimeMillis() + (172800L * 1000L))
                                .build();
                sessionCachePort.saveQuickRevokeToken(quickRevokeToken, 172800L);

                String baseUrl = resolveBaseUrl(command.getTenantId());
                revokeUrl = baseUrl + "/api/v1/auth/device/quick-revoke?token="
                        + quickRevokeTokenStr + "&blacklist=true";
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", command.getUsername() != null ? command.getUsername() : "");
            variables.put("appName", "KeepGuard");
            variables.put("updatedAt", LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            variables.put("deviceName", command.getDeviceName() != null && !command.getDeviceName().isBlank()
                    ? command.getDeviceName() : "Dispositivo Desconhecido");
            variables.put("deviceType", command.getDeviceType() != null && !command.getDeviceType().isBlank()
                    ? command.getDeviceType() : "DESKTOP");
            variables.put("ipAddress", command.getIpAddress() != null && !command.getIpAddress().isBlank()
                    ? command.getIpAddress() : "IP não identificado");
            variables.put("userAgent", command.getUserAgent() != null && !command.getUserAgent().isBlank()
                    ? command.getUserAgent() : "N/A");
            variables.put("quickRevokeToken", quickRevokeTokenStr != null ? quickRevokeTokenStr : "");
            variables.put("revokeUrl", revokeUrl);

            Map<String, Object> msgPayload = Map.of(
                    "recipient", command.getEmail(),
                    "codeUser", command.getCodeUser() != null ? command.getCodeUser() : "",
                    "templateType", "SENHA_ALTERADA_SUCESSO",
                    "messageType", "EMAIL",
                    "communicationType", "EMAIL",
                    "subject", "Alerta de Segurança: Senha da sua conta foi alterada",
                    "variables", variables
            );

            communicationClient.sendMessage(msgPayload, command.getTenantId());
            log.info("Notificação de senha alterada enviada | codeUser={} | email={} | hasRevokeUrl={}",
                    command.getCodeUser(), command.getEmail(), !revokeUrl.isBlank());
        } catch (Exception e) {
            log.error("Falha ao enviar notificação de senha alterada: {}", e.getMessage(), e);
        }
    }

    private String resolveBaseUrl(String tenantId) {
        try {
            if (companyClient != null && tenantId != null && !tenantId.isBlank()) {
                Map<String, Object> company = companyClient.getCompanyByTenantId(tenantId);
                if (company != null) {
                    if (company.containsKey("apiBaseUrl") && company.get("apiBaseUrl") != null) {
                        return (String) company.get("apiBaseUrl");
                    }
                    if (company.containsKey("frontendBaseUrl") && company.get("frontendBaseUrl") != null) {
                        return (String) company.get("frontendBaseUrl");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível carregar URLs customizadas da Company | tenantId={} | usando default: {}",
                    tenantId, defaultApiBaseUrl);
        }
        return defaultApiBaseUrl != null ? defaultApiBaseUrl : "http://localhost:8381";
    }

    public List<DeviceSessionView> listUserSessions(String codeUser, String currentDeviceId) {
        return listUserSessions(codeUser, currentDeviceId, null, null);
    }

    public List<DeviceSessionView> listUserSessions(String codeUser, String currentDeviceId, String requestIp) {
        return listUserSessions(codeUser, currentDeviceId, requestIp, null);
    }

    public List<DeviceSessionView> listUserSessions(String codeUser, String currentDeviceId, String requestIp, String requestLocation) {
        List<UserSession> activeSessions = sessionCachePort.listUserSessions(codeUser);
        Map<String, UserSession> activeSessionMap = activeSessions.stream()
                .filter(s -> s.getDeviceId() != null)
                .collect(Collectors.toMap(UserSession::getDeviceId, s -> s, (s1, s2) -> s1));

        List<DeviceSessionView> result = new ArrayList<>();

        // Se houver registros no PostgreSQL, usa como fonte primária enriquecida com o status ativo do Redis
        try {
            UUID codeUserUuid = UUID.fromString(codeUser);
            List<UserDevice> devices = userDeviceRepository.listByCodeUser(codeUserUuid);
            if (!devices.isEmpty()) {
                for (UserDevice dev : devices) {
                    if (dev.getRevokedAt() != null) {
                        continue; // Ignora dispositivos com sessão revogada
                    }
                    UserSession active = activeSessionMap.get(dev.getDeviceId());
                    boolean isCurrent = dev.getDeviceId() != null && dev.getDeviceId().equals(currentDeviceId);
                    String ipAddress = resolveSessionIp(
                            dev.getIpAddress(),
                            active != null ? active.getIpAddress() : null,
                            isCurrent ? requestIp : null
                    );
                    persistDeviceIpIfMissing(dev, ipAddress);
                    String location = resolveAndPersistLocation(active, ipAddress, isCurrent ? requestLocation : null);

                    result.add(new DeviceSessionView(
                            active != null ? active.getSessionId() : "device_" + dev.getDeviceId(),
                            dev.getDeviceId(),
                            dev.getDeviceName(),
                            dev.getDeviceType(),
                            ipAddress,
                            location,
                            isCurrent,
                            dev.getIsTrusted(),
                            dev.getLastActiveAt() != null ? dev.getLastActiveAt().toString() : null,
                            dev.getFirstSeenAt() != null ? dev.getFirstSeenAt().toString() : null
                    ));
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("Falha ao consultar dispositivos no PostgreSQL, fallback para Redis | codeUser={} | erro={}", codeUser, e.getMessage());
        }

        // Fallback para Redis se banco vazio ou indisponível
        return activeSessions.stream().map(s -> {
            boolean isCurrent = s.getDeviceId() != null && s.getDeviceId().equals(currentDeviceId);
            String ipAddress = resolveSessionIp(s.getIpAddress(), null, isCurrent ? requestIp : null);
            return new DeviceSessionView(
                    s.getSessionId(),
                    s.getDeviceId(),
                    s.getDeviceName(),
                    s.getDeviceType(),
                    ipAddress,
                    resolveAndPersistLocation(s, ipAddress, isCurrent ? requestLocation : null),
                    isCurrent,
                    s.getIsTrusted(),
                    s.getLastActiveAt(),
                    s.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    private String resolveAndPersistLocation(UserSession session, String ipAddress, String preferredLocation) {
        String preferred = ClientLocation.sanitize(preferredLocation);
        if (ClientLocation.isUsable(preferred)) {
            if (session != null && !preferred.equals(session.getLocation())) {
                session.setLocation(preferred);
                sessionCachePort.saveUserSession(session, 2592000L);
            }
            return preferred;
        }
        String ip = IpAddressUtils.firstIp(ipAddress);
        boolean publicIp = ip != null && !IpAddressUtils.isPrivate(ip);
        String existing = session != null ? session.getLocation() : null;
        boolean staleLocation = existing == null || existing.isBlank()
                || "Localização Desconhecida".equalsIgnoreCase(existing)
                || "Rede interna".equalsIgnoreCase(existing);
        boolean replacingPrivateSession = publicIp
                && session != null
                && IpAddressUtils.isPrivate(session.getIpAddress());
        boolean replacingIpv6WithIpv4 = publicIp
                && IpAddressUtils.isIpv4(ip)
                && session != null
                && IpAddressUtils.isIpv6(session.getIpAddress());
        if (!staleLocation && !replacingPrivateSession && !replacingIpv6WithIpv4) {
            return existing;
        }
        String resolved = geoLocationPort.resolve(ip);
        boolean resolvedUsable = ClientLocation.isUsable(resolved);
        if (!resolvedUsable) {
            return ClientLocation.isUsable(existing) ? existing : resolved;
        }
        boolean shouldPersist = session != null && (staleLocation
                || replacingPrivateSession
                || replacingIpv6WithIpv4
                || !resolved.equals(existing));
        if (shouldPersist) {
            session.setLocation(resolved);
            if (ip != null && (session.getIpAddress() == null || session.getIpAddress().isBlank()
                    || (publicIp && IpAddressUtils.isPrivate(session.getIpAddress()))
                    || replacingIpv6WithIpv4)) {
                session.setIpAddress(ip);
            }
            sessionCachePort.saveUserSession(session, 2592000L);
        }
        return resolved;
    }

    private void persistDeviceIpIfMissing(UserDevice device, String ipAddress) {
        if (device == null) {
            return;
        }
        String incoming = IpAddressUtils.firstIp(ipAddress);
        if (incoming == null) {
            return;
        }
        String current = device.getIpAddress();
        boolean shouldWrite = current == null || current.isBlank()
                || (IpAddressUtils.isPrivate(current) && !IpAddressUtils.isPrivate(incoming))
                || (IpAddressUtils.isIpv6(current) && IpAddressUtils.isIpv4(incoming));
        if (!shouldWrite) {
            return;
        }
        device.updateActivity(incoming, null, device.getLastActiveAt());
        userDeviceRepository.save(device);
    }

    private static String resolveSessionIp(String deviceIp, String sessionIp, String requestIp) {
        String requestPublic = IpAddressUtils.firstPublic(requestIp);
        if (requestPublic != null) {
            String stored = firstNonBlank(deviceIp, sessionIp);
            if (stored == null || IpAddressUtils.isPrivate(stored)
                    || (IpAddressUtils.isIpv6(stored) && IpAddressUtils.isIpv4(requestPublic))) {
                return requestPublic;
            }
        }
        return firstNonBlank(deviceIp, sessionIp, requestIp);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String ip = IpAddressUtils.firstIp(value);
            if (ip != null && !ip.isBlank()) {
                return ip;
            }
        }
        return null;
    }

    public void revokeSession(String codeUser, String deviceId) {
        // Remove token JWT correspondente do Redis se existir
        sessionCachePort.getUserSession(codeUser, deviceId).ifPresent(s -> {
            if (s.getRefreshToken() != null && !s.getRefreshToken().isBlank()) {
                tokenCachePort.removeToken(codeUser, s.getRefreshToken());
            }
        });

        sessionCachePort.removeUserSession(codeUser, deviceId);
        try {
            userDeviceRepository.findByCodeUserAndDeviceId(UUID.fromString(codeUser), deviceId)
                    .ifPresent(dev -> {
                        dev.revoke(LocalDateTime.now());
                        userDeviceRepository.save(dev);
                    });
        } catch (Exception e) {
            log.warn("Falha ao atualizar revogação do dispositivo no banco | codeUser={} | deviceId={} | erro={}",
                    codeUser, deviceId, e.getMessage());
        }
        log.info("Sessão revogada remotamente | codeUser={} | deviceId={}", codeUser, deviceId);
    }

    public void revokeAllOtherSessions(String codeUser, String currentDeviceId) {
        // Localiza e remove os tokens de todas as outras sessões do Redis
        List<UserSession> activeSessions = sessionCachePort.listUserSessions(codeUser);
        for (UserSession s : activeSessions) {
            if (s.getDeviceId() != null && !s.getDeviceId().equals(currentDeviceId)) {
                if (s.getRefreshToken() != null && !s.getRefreshToken().isBlank()) {
                    tokenCachePort.removeToken(codeUser, s.getRefreshToken());
                }
            }
        }

        sessionCachePort.removeAllUserSessionsExceptCurrent(codeUser, currentDeviceId);
        try {
            UUID codeUserUuid = UUID.fromString(codeUser);
            List<UserDevice> devices = userDeviceRepository.listByCodeUser(codeUserUuid);
            for (UserDevice dev : devices) {
                if (dev.getDeviceId() != null && !dev.getDeviceId().equals(currentDeviceId)) {
                    dev.revoke(LocalDateTime.now());
                    userDeviceRepository.save(dev);
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao atualizar revogação de outras sessões no banco | codeUser={} | currentDeviceId={} | erro={}",
                    codeUser, currentDeviceId, e.getMessage());
        }
        log.info("Todas as outras sessões foram revogadas | codeUser={} | currentDeviceId={}", codeUser, currentDeviceId);
    }

    public void revokeAllSessions(String codeUser) {
        // Localiza e remove os tokens de todas as sessões do Redis
        List<UserSession> activeSessions = sessionCachePort.listUserSessions(codeUser);
        for (UserSession s : activeSessions) {
            if (s.getRefreshToken() != null && !s.getRefreshToken().isBlank()) {
                tokenCachePort.removeToken(codeUser, s.getRefreshToken());
            }
        }

        // Limpa todas as sessões e tokens legados do cache
        sessionCachePort.removeAllUserSessions(codeUser);
        tokenCachePort.removeAllTokens(codeUser);

        try {
            UUID codeUserUuid = UUID.fromString(codeUser);
            List<UserDevice> devices = userDeviceRepository.listByCodeUser(codeUserUuid);
            for (UserDevice dev : devices) {
                dev.revoke(LocalDateTime.now());
                userDeviceRepository.save(dev);
            }
        } catch (Exception e) {
            log.warn("Falha ao atualizar revogação total de sessões no banco | codeUser={} | erro={}",
                    codeUser, e.getMessage());
        }
        log.info("Todas as sessões e tokens foram revogados completamente | codeUser={}", codeUser);
    }

    public Map<String, Object> quickRevoke(String token, boolean addToBlacklist) {
        log.info("Processando revogação rápida via link de e-mail | token={}", token);
        com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken quickRevokeToken = sessionCachePort.getQuickRevokeToken(token)
                .orElseThrow(() -> new NotFoundException("Link de revogação inválido ou expirado.", "INVALID_REVOKE_TOKEN", Map.of()));

        String codeUser = quickRevokeToken.getCodeUser();
        String deviceId = quickRevokeToken.getDeviceId();

        // 1. Revogar a sessão do dispositivo associado (Redis + DB)
        sessionCachePort.removeUserSession(codeUser, deviceId);
        try {
            userDeviceRepository.findByCodeUserAndDeviceId(UUID.fromString(codeUser), deviceId)
                    .ifPresent(dev -> {
                        dev.revoke(LocalDateTime.now());
                        userDeviceRepository.save(dev);
                    });
        } catch (Exception e) {
            log.warn("Falha ao atualizar revogação de dispositivo no banco (quickRevoke) | codeUser={} | deviceId={} | erro={}",
                    codeUser, deviceId, e.getMessage());
        }

        // 2. Adicionar à blacklist se solicitado (Redis + DB)
        if (addToBlacklist) {
            UUID tenantIdUuid = null;
            if (quickRevokeToken.getTenantId() != null && !quickRevokeToken.getTenantId().isBlank()) {
                try {
                    tenantIdUuid = UUID.fromString(quickRevokeToken.getTenantId());
                } catch (Exception ignored) {}
            }
            if (tenantIdUuid == null) {
                try {
                    tenantIdUuid = userRepository.findByCodeUser(UUID.fromString(codeUser))
                            .map(User::getTenantId)
                            .orElse(null);
                } catch (Exception ignored) {}
            }

            com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry entry = com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry.builder()
                    .tenantId(tenantIdUuid)
                    .codeUser(codeUser)
                    .deviceId(deviceId)
                    .deviceName(quickRevokeToken.getDeviceName())
                    .ipAddress(quickRevokeToken.getIpAddress())
                    .userAgent(quickRevokeToken.getUserAgent())
                    .reason("Revogação rápida via e-mail de alerta de segurança")
                    .blockedAt(LocalDateTime.now().toString())
                    .blockedBy(codeUser)
                    .build();
            sessionCachePort.addToBlacklist(entry, 0); // persistente no Redis

            try {
                deviceBlacklistRepository.save(entry);
            } catch (Exception e) {
                log.warn("Falha ao persistir blacklist no PostgreSQL (quickRevoke) | codeUser={} | deviceId={} | erro={}",
                        codeUser, deviceId, e.getMessage());
            }
        }

        // 3. Remover token de revogação rápida após o uso
        sessionCachePort.removeQuickRevokeToken(token);

        log.info("Sessão encerrada com sucesso via quick-revoke | codeUser={} | deviceId={} | blacklisted={}",
                codeUser, deviceId, addToBlacklist);

        return Map.of(
                "message", "Sessão do dispositivo revogada com sucesso.",
                "deviceId", deviceId,
                "blacklisted", addToBlacklist
        );
    }

    public void addDeviceToBlacklist(String codeUser, String deviceId, String deviceName, String reason) {
        // Encerra sessão do dispositivo se houver
        sessionCachePort.removeUserSession(codeUser, deviceId);
        try {
            userDeviceRepository.findByCodeUserAndDeviceId(UUID.fromString(codeUser), deviceId)
                    .ifPresent(dev -> {
                        dev.revoke(LocalDateTime.now());
                        userDeviceRepository.save(dev);
                    });
        } catch (Exception e) {
            log.warn("Falha ao atualizar revogação do dispositivo ao adicionar à blacklist | codeUser={} | deviceId={}", codeUser, deviceId);
        }

        UUID tenantIdUuid = null;
        String ipAddress = null;
        String userAgent = null;
        try {
            Optional<UserDevice> userDeviceOpt = userDeviceRepository.findByCodeUserAndDeviceId(UUID.fromString(codeUser), deviceId);
            if (userDeviceOpt.isPresent()) {
                UserDevice dev = userDeviceOpt.get();
                tenantIdUuid = dev.getTenantId();
                ipAddress = dev.getIpAddress();
                userAgent = dev.getUserAgent();
                if (deviceName == null || deviceName.isBlank()) {
                    deviceName = dev.getDeviceName();
                }
            }
        } catch (Exception ignored) {}

        if (tenantIdUuid == null) {
            try {
                tenantIdUuid = userRepository.findByCodeUser(UUID.fromString(codeUser))
                        .map(User::getTenantId)
                        .orElse(null);
            } catch (Exception ignored) {}
        }

        com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry entry = com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry.builder()
                .tenantId(tenantIdUuid)
                .codeUser(codeUser)
                .deviceId(deviceId)
                .deviceName(deviceName != null ? deviceName : "Dispositivo Bloqueado")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .reason(reason != null ? reason : "Bloqueado pelo usuário")
                .blockedAt(LocalDateTime.now().toString())
                .blockedBy(codeUser)
                .build();

        sessionCachePort.addToBlacklist(entry, 0);

        try {
            deviceBlacklistRepository.save(entry);
        } catch (Exception e) {
            log.warn("Falha ao salvar blacklist no PostgreSQL | codeUser={} | deviceId={} | erro={}", codeUser, deviceId, e.getMessage());
        }

        log.info("Dispositivo adicionado à blacklist | codeUser={} | deviceId={}", codeUser, deviceId);
    }

    public List<com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry> listBlacklist(String codeUser) {
        try {
            List<com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry> dbList = deviceBlacklistRepository.listByCodeUser(UUID.fromString(codeUser));
            if (!dbList.isEmpty()) {
                return dbList;
            }
        } catch (Exception e) {
            log.warn("Falha ao listar blacklist no PostgreSQL, fallback para Redis | codeUser={} | erro={}", codeUser, e.getMessage());
        }
        return sessionCachePort.listBlacklistedDevices(codeUser);
    }

    public void removeDeviceFromBlacklist(String codeUser, String deviceId) {
        sessionCachePort.removeFromBlacklist(codeUser, deviceId);
        try {
            deviceBlacklistRepository.deleteByCodeUserAndDeviceId(UUID.fromString(codeUser), deviceId);
        } catch (Exception e) {
            log.warn("Falha ao remover blacklist do PostgreSQL | codeUser={} | deviceId={} | erro={}", codeUser, deviceId, e.getMessage());
        }
        log.info("Dispositivo removido da blacklist | codeUser={} | deviceId={}", codeUser, deviceId);
    }

    public org.springframework.data.domain.Page<com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry> searchBlacklist(
            UUID tenantId, UUID codeUser, String deviceId, String deviceName, String ipAddress,
            java.time.LocalDateTime from, java.time.LocalDateTime to, org.springframework.data.domain.Pageable pageable) {
        return deviceBlacklistRepository.search(tenantId, codeUser, deviceId, deviceName, ipAddress, from, to, pageable);
    }

    public void adminAddDeviceToBlacklist(UUID tenantId, String codeUser, String deviceId, String deviceName, String reason, String blockedBy, LocalDateTime expiresAt) {
        sessionCachePort.removeUserSession(codeUser, deviceId);
        try {
            userDeviceRepository.findByCodeUserAndDeviceId(UUID.fromString(codeUser), deviceId)
                    .ifPresent(dev -> {
                        dev.revoke(LocalDateTime.now());
                        userDeviceRepository.save(dev);
                    });
        } catch (Exception ignored) {}

        String ipAddress = null;
        String userAgent = null;
        try {
            Optional<UserDevice> userDeviceOpt = userDeviceRepository.findByCodeUserAndDeviceId(UUID.fromString(codeUser), deviceId);
            if (userDeviceOpt.isPresent()) {
                UserDevice dev = userDeviceOpt.get();
                ipAddress = dev.getIpAddress();
                userAgent = dev.getUserAgent();
                if (deviceName == null || deviceName.isBlank()) {
                    deviceName = dev.getDeviceName();
                }
            }
        } catch (Exception ignored) {}

        com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry entry = com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry.builder()
                .tenantId(tenantId)
                .codeUser(codeUser)
                .deviceId(deviceId)
                .deviceName(deviceName != null ? deviceName : "Dispositivo Bloqueado")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .reason(reason != null ? reason : "Bloqueado pelo Administrador")
                .blockedAt(LocalDateTime.now().toString())
                .blockedBy(blockedBy)
                .expiresAt(expiresAt)
                .build();

        sessionCachePort.addToBlacklist(entry, 0);

        try {
            deviceBlacklistRepository.save(entry);
        } catch (Exception e) {
            log.warn("Falha ao salvar blacklist administrativo no PostgreSQL | codeUser={} | deviceId={} | erro={}", codeUser, deviceId, e.getMessage());
        }

        log.info("Dispositivo adicionado à blacklist por Admin | tenantId={} | codeUser={} | deviceId={} | blockedBy={}",
                tenantId, codeUser, deviceId, blockedBy);
    }

    public void adminRemoveDeviceFromBlacklist(UUID tenantId, String codeUser, String deviceId) {
        sessionCachePort.removeFromBlacklist(codeUser, deviceId);
        try {
            deviceBlacklistRepository.deleteByCodeUserAndDeviceId(UUID.fromString(codeUser), deviceId);
        } catch (Exception e) {
            log.warn("Falha ao remover blacklist administrativo do PostgreSQL | codeUser={} | deviceId={} | erro={}", codeUser, deviceId, e.getMessage());
        }
        log.info("Dispositivo removido da blacklist por Admin | tenantId={} | codeUser={} | deviceId={}", tenantId, codeUser, deviceId);
    }

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

    private List<String> getUserAuthorities(UUID userId) {
        List<UUID> roleIds = userRoleRepository.findByUserId(userId)
                .stream()
                .map(userRole -> userRole.getRoleId())
                .toList();

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

}
