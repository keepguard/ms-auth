package com.keepguard.ms_auth.application.service.session;

import com.keepguard.lib_common.utils.CodeGeneratorUtils;
import com.keepguard.ms_auth.adapters.out.feign.CommunicationClient;
import com.keepguard.ms_auth.adapters.out.feign.UserClient;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.session.DeviceSessionView;
import com.keepguard.ms_auth.application.dto.session.SendDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.dto.session.VerifyDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.port.out.cache.SessionCachePort;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.session.DeviceChallengeSession;
import com.keepguard.ms_auth.domain.entity.session.UserSession;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserClient userClient;
    private final JwtService jwtService;

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
                    "recipient", recipient != null ? recipient : challenge.getEmail(),
                    "codeUser", challenge.getCodeUser(),
                    "templateType", templateType,
                    "messageType", messageType,
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
        String displayHandle = getDisplayHandle(user.getCodeUser(), challenge.getTenantId());

        // Emitir JWT final
        String token = jwtService.generateToken(user, roleNames, authorities, challenge.getTenantId(), challenge.getClientId(), displayHandle, challenge.getDeviceId());

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Salva token legado
        tokenCachePort.saveToken(user.getCodeUser().toString(), token, jwtService.getExpiration());

        // Registra a sessão do dispositivo no Redis
        UserSession session = UserSession.builder()
                .sessionId("sess_" + UUID.randomUUID())
                .codeUser(challenge.getCodeUser())
                .tenantId(challenge.getTenantId())
                .clientId(challenge.getClientId())
                .deviceId(challenge.getDeviceId())
                .deviceName(challenge.getDeviceName())
                .deviceType(challenge.getDeviceType())
                .ipAddress(challenge.getIpAddress())
                .userAgent(challenge.getUserAgent())
                .isTrusted(Boolean.TRUE.equals(command.getTrustDevice()))
                .lastActiveAt(LocalDateTime.now().toString())
                .createdAt(LocalDateTime.now().toString())
                .build();

        sessionCachePort.saveUserSession(session, 2592000L); // 30 dias

        // Remove o challenge
        sessionCachePort.removeDeviceChallenge(command.getChallengeSessionId());

        log.info("Dispositivo verificado com sucesso! Sessão ativada | codeUser={} | deviceId={}", challenge.getCodeUser(), challenge.getDeviceId());

        return new AuthLoginView(token, 3600L, "AUTHENTICATED", null, true, null);
    }

    public List<DeviceSessionView> listUserSessions(String codeUser, String currentDeviceId) {
        List<UserSession> sessions = sessionCachePort.listUserSessions(codeUser);
        return sessions.stream().map(s -> new DeviceSessionView(
                s.getSessionId(),
                s.getDeviceId(),
                s.getDeviceName(),
                s.getDeviceType(),
                s.getIpAddress(),
                s.getLocation() != null ? s.getLocation() : "Localização Desconhecida",
                s.getDeviceId() != null && s.getDeviceId().equals(currentDeviceId),
                s.getIsTrusted(),
                s.getLastActiveAt(),
                s.getCreatedAt()
        )).collect(Collectors.toList());
    }

    public void revokeSession(String codeUser, String deviceId) {
        sessionCachePort.removeUserSession(codeUser, deviceId);
        log.info("Sessão revogada remotamente | codeUser={} | deviceId={}", codeUser, deviceId);
    }

    public void revokeAllOtherSessions(String codeUser, String currentDeviceId) {
        sessionCachePort.removeAllUserSessionsExceptCurrent(codeUser, currentDeviceId);
        log.info("Todas as outras sessões foram revogadas | codeUser={} | currentDeviceId={}", codeUser, currentDeviceId);
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

    private String getDisplayHandle(UUID codeUser, String tenantId) {
        try {
            Map<String, Object> userData = userClient.getUserByCode(codeUser, tenantId);
            if (userData != null && userData.containsKey("display_handle") && userData.get("display_handle") != null) {
                return (String) userData.get("display_handle");
            }
        } catch (Exception e) {
            log.warn("Não foi possível buscar display_handle do ms-user | codeUser={} | erro={}", codeUser, e.getMessage());
        }
        return null;
    }
}
