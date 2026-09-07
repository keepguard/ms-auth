package com.keepguard.ms_auth.adapters.in.rest.session;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLoginResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.request.DeviceBlacklistRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.request.SendDeviceChallengeRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.request.VerifyDeviceChallengeRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.response.DeviceBlacklistResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.response.DeviceSessionResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.mapper.DeviceSessionAdapterMapper;
import com.keepguard.ms_auth.application.port.in.DeviceSessionPort;
import com.keepguard.ms_auth.infrastructure.util.ClientIpResolver;
import com.keepguard.ms_auth.infrastructure.util.ClientLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device & Sessions", description = "Endpoints para desafio MFA de novo dispositivo e gestão de sessões ativas")
public class DeviceSessionController {

    private final DeviceSessionPort deviceSessionPort;
    private final DeviceSessionAdapterMapper mapper;

    @PostMapping("/auth/device/challenge/send")
    @Operation(summary = "Enviar código de verificação para novo dispositivo",
               description = "Dispara o envio do OTP para o canal selecionado pelo usuário (EMAIL, SMS, WHATSAPP)")
    @MetricsEndpoint(endpoint = "auth_device_challenge_send")
    public ResponseEntity<Map<String, Object>> sendChallenge(
            @Valid @RequestBody SendDeviceChallengeRequestDTO request,
            @RequestHeader("X-Company-Id") UUID companyId) {

        Map<String, Object> response = deviceSessionPort.sendChallenge(mapper.toSendChallengeCommand(request, companyId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/device/challenge/verify")
    @Operation(summary = "Validar código de verificação e ativar sessão do dispositivo",
               description = "Valida o OTP de 6 dígitos, positiva o dispositivo e emite o JWT final")
    @MetricsEndpoint(endpoint = "auth_device_challenge_verify")
    public ResponseEntity<AuthLoginResponseDTO> verifyChallenge(
            @Valid @RequestBody VerifyDeviceChallengeRequestDTO request,
            @RequestHeader("X-Company-Id") UUID companyId) {

        return ResponseEntity.ok(mapper.toLoginResponse(
                deviceSessionPort.verifyChallenge(mapper.toVerifyChallengeCommand(request, companyId))));
    }

    @GetMapping("/users/me/sessions")
    @Operation(summary = "Listar dispositivos conectados",
               description = "Retorna a lista de todas as sessões e aparelhos ativos na conta do usuário logado")
    @MetricsEndpoint(endpoint = "users_list_sessions")
    public ResponseEntity<List<DeviceSessionResponseDTO>> listSessions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Device-Id", required = false) String currentDeviceId,
            HttpServletRequest httpRequest) {

        String codeUser = jwt.getSubject();
        String deviceId = currentDeviceId != null ? currentDeviceId : jwt.getClaimAsString("device_id");
        return ResponseEntity.ok(mapper.toSessionResponseList(deviceSessionPort.listUserSessions(
                codeUser, deviceId, ClientIpResolver.from(httpRequest), ClientLocation.from(httpRequest))));
    }

    @DeleteMapping("/users/me/sessions/{deviceId}")
    @Operation(summary = "Desconectar dispositivo específico",
               description = "Revoga remotamente a sessão de um dispositivo")
    @MetricsEndpoint(endpoint = "users_revoke_session")
    public ResponseEntity<Void> revokeSession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("deviceId") String deviceId) {

        deviceSessionPort.revokeSession(jwt.getSubject(), deviceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/me/sessions")
    @Operation(summary = "Desconectar todas as outras sessões",
               description = "Revoga todas as sessões ativas exceto a do dispositivo atual")
    @MetricsEndpoint(endpoint = "users_revoke_other_sessions")
    public ResponseEntity<Void> revokeOtherSessions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Device-Id", required = false) String currentDeviceId) {

        String deviceId = currentDeviceId != null ? currentDeviceId : jwt.getClaimAsString("device_id");
        deviceSessionPort.revokeAllOtherSessions(jwt.getSubject(), deviceId);
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

        return ResponseEntity.ok(deviceSessionPort.quickRevoke(token, blacklist));
    }

    @GetMapping("/users/me/devices/blacklist")
    @Operation(summary = "Listar dispositivos na blacklist do usuário",
               description = "Retorna todos os dispositivos bloqueados para o usuário autenticado")
    @MetricsEndpoint(endpoint = "users_list_device_blacklist")
    public ResponseEntity<List<DeviceBlacklistResponseDTO>> listBlacklist(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(mapper.toBlacklistResponseList(deviceSessionPort.listBlacklist(jwt.getSubject())));
    }

    @PostMapping("/users/me/devices/blacklist")
    @Operation(summary = "Adicionar dispositivo à blacklist",
               description = "Bloqueia um dispositivo para impedir novos logins e encerra sessão ativa se houver")
    @MetricsEndpoint(endpoint = "users_add_device_blacklist")
    public ResponseEntity<Void> addDeviceToBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody DeviceBlacklistRequestDTO request) {

        DeviceBlacklistRequestDTO body = mapper.emptyIfNull(request);
        if (body.getDeviceId() == null || body.getDeviceId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        deviceSessionPort.addDeviceToBlacklist(jwt.getSubject(), body.getDeviceId(), body.getDeviceName(), body.getReason());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/me/devices/blacklist/{deviceId}")
    @Operation(summary = "Remover dispositivo da blacklist",
               description = "Desbloqueia um dispositivo previamente colocado na blacklist")
    @MetricsEndpoint(endpoint = "users_remove_device_blacklist")
    public ResponseEntity<Void> removeDeviceFromBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("deviceId") String deviceId) {

        deviceSessionPort.removeDeviceFromBlacklist(jwt.getSubject(), deviceId);
        return ResponseEntity.noContent().build();
    }
}
