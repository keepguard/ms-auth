package com.keepguard.ms_auth.adapters.in.rest.session;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.lib_common.utils.ValidationUtils;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLoginResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.mapper.AuthAdapterMapper;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.session.DeviceSessionView;
import com.keepguard.ms_auth.application.dto.session.SendDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.dto.session.VerifyDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.service.session.DeviceSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device & Sessions", description = "Endpoints para desafio MFA de novo dispositivo e gestão de sessões ativas")
public class DeviceSessionController {

    private final DeviceSessionService deviceSessionService;
    private final AuthAdapterMapper authAdapterMapper;

    @PostMapping("/auth/device/challenge/send")
    @Operation(summary = "Enviar código de verificação para novo dispositivo",
               description = "Dispara o envio do OTP para o canal selecionado pelo usuário (EMAIL, SMS, WHATSAPP)")
    @MetricsEndpoint(endpoint = "auth_device_challenge_send")
    public ResponseEntity<Map<String, Object>> sendChallenge(
            @Valid @RequestBody SendDeviceChallengeCommandDTO request,
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        request.setTenantId(tenantIdHeader);
        Map<String, Object> response = deviceSessionService.sendChallenge(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/device/challenge/verify")
    @Operation(summary = "Validar código de verificação e ativar sessão do dispositivo",
               description = "Valida o OTP de 6 dígitos, positiva o dispositivo e emite o JWT final")
    @MetricsEndpoint(endpoint = "auth_device_challenge_verify")
    public ResponseEntity<AuthLoginResponseDTO> verifyChallenge(
            @Valid @RequestBody VerifyDeviceChallengeCommandDTO request,
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        request.setTenantId(tenantIdHeader);
        AuthLoginView view = deviceSessionService.verifyChallenge(request);
        AuthLoginResponseDTO response = authAdapterMapper.toLoginResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/me/sessions")
    @Operation(summary = "Listar dispositivos conectados",
               description = "Retorna a lista de todas as sessões e aparelhos ativos na conta do usuário logado")
    @MetricsEndpoint(endpoint = "users_list_sessions")
    public ResponseEntity<List<DeviceSessionView>> listSessions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Device-Id", required = false) String currentDeviceId) {

        String codeUser = jwt.getSubject();
        String deviceId = currentDeviceId != null ? currentDeviceId : jwt.getClaimAsString("device_id");
        List<DeviceSessionView> sessions = deviceSessionService.listUserSessions(codeUser, deviceId);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/users/me/sessions/{deviceId}")
    @Operation(summary = "Desconectar dispositivo específico",
               description = "Revoga remotamente a sessão de um dispositivo")
    @MetricsEndpoint(endpoint = "users_revoke_session")
    public ResponseEntity<Void> revokeSession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("deviceId") String deviceId) {

        String codeUser = jwt.getSubject();
        deviceSessionService.revokeSession(codeUser, deviceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/me/sessions")
    @Operation(summary = "Desconectar todas as outras sessões",
               description = "Revoga todas as sessões ativas exceto a do dispositivo atual")
    @MetricsEndpoint(endpoint = "users_revoke_other_sessions")
    public ResponseEntity<Void> revokeOtherSessions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Device-Id", required = false) String currentDeviceId) {

        String codeUser = jwt.getSubject();
        String deviceId = currentDeviceId != null ? currentDeviceId : jwt.getClaimAsString("device_id");
        deviceSessionService.revokeAllOtherSessions(codeUser, deviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/device/quick-revoke")
    @PostMapping("/auth/device/quick-revoke")
    @Operation(summary = "Revogação rápida de dispositivo via link/token de e-mail",
               description = "Permite revogar a sessão de um dispositivo conectado usando o token assinado enviado por e-mail, com opção de adicionar à blacklist")
    @MetricsEndpoint(endpoint = "auth_device_quick_revoke")
    public ResponseEntity<Map<String, Object>> quickRevoke(
            @RequestParam("token") String token,
            @RequestParam(value = "blacklist", defaultValue = "true") boolean blacklist) {

        Map<String, Object> result = deviceSessionService.quickRevoke(token, blacklist);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/me/devices/blacklist")
    @Operation(summary = "Listar dispositivos na blacklist do usuário",
               description = "Retorna todos os dispositivos bloqueados para o usuário autenticado")
    @MetricsEndpoint(endpoint = "users_list_device_blacklist")
    public ResponseEntity<List<com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry>> listBlacklist(
            @AuthenticationPrincipal Jwt jwt) {

        String codeUser = jwt.getSubject();
        List<com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry> list = deviceSessionService.listBlacklist(codeUser);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/users/me/devices/blacklist")
    @Operation(summary = "Adicionar dispositivo à blacklist",
               description = "Bloqueia um dispositivo para impedir novos logins e encerra sessão ativa se houver")
    @MetricsEndpoint(endpoint = "users_add_device_blacklist")
    public ResponseEntity<Void> addDeviceToBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> request) {

        String codeUser = jwt.getSubject();
        String deviceId = request.get("deviceId");
        String deviceName = request.get("deviceName");
        String reason = request.get("reason");

        if (deviceId == null || deviceId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        deviceSessionService.addDeviceToBlacklist(codeUser, deviceId, deviceName, reason);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/me/devices/blacklist/{deviceId}")
    @Operation(summary = "Remover dispositivo da blacklist",
               description = "Desbloqueia um dispositivo previamente colocado na blacklist")
    @MetricsEndpoint(endpoint = "users_remove_device_blacklist")
    public ResponseEntity<Void> removeDeviceFromBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("deviceId") String deviceId) {

        String codeUser = jwt.getSubject();
        deviceSessionService.removeDeviceFromBlacklist(codeUser, deviceId);
        return ResponseEntity.noContent().build();
    }
}
