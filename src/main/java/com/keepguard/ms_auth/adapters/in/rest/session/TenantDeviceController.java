package com.keepguard.ms_auth.adapters.in.rest.session;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.request.DeviceBlacklistRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.response.DeviceBlacklistResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.response.DeviceSessionResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.mapper.DeviceSessionAdapterMapper;
import com.keepguard.ms_auth.application.port.in.DeviceSessionPort;
import com.keepguard.ms_auth.infrastructure.util.ClientIpResolver;
import com.keepguard.ms_auth.infrastructure.util.ClientLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Tenant Devices & Sessions", description = "Gestão de sessões e blacklist no escopo do tenant")
public class TenantDeviceController {

    private final DeviceSessionPort deviceSessionPort;
    private final DeviceSessionAdapterMapper mapper;

    @GetMapping("/users/{userId}/sessions")
    @Operation(summary = "Listar sessões de um usuário do tenant")
    @MetricsEndpoint(endpoint = "tenant_list_user_sessions")
    public ResponseEntity<List<DeviceSessionResponseDTO>> listUserSessions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable("userId") String userId,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(mapper.toSessionResponseList(deviceSessionPort.listSessionsForUser(
                companyId,
                actorCodeUser(jwt),
                userId,
                null,
                ClientIpResolver.from(httpRequest),
                ClientLocation.from(httpRequest)
        )));
    }

    @DeleteMapping("/users/{userId}/sessions/{deviceId}")
    @Operation(summary = "Revogar sessão de um usuário do tenant")
    @MetricsEndpoint(endpoint = "tenant_revoke_user_session")
    public ResponseEntity<Void> revokeUserSession(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable("userId") String userId,
            @PathVariable("deviceId") String deviceId) {

        deviceSessionPort.revokeSessionForUser(companyId, actorCodeUser(jwt), userId, deviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/devices/blacklist")
    @Operation(summary = "Listar blacklist de um usuário do tenant")
    @MetricsEndpoint(endpoint = "tenant_list_user_blacklist")
    public ResponseEntity<List<DeviceBlacklistResponseDTO>> listUserBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable("userId") String userId) {

        return ResponseEntity.ok(mapper.toTenantBlacklistResponseList(
                deviceSessionPort.listBlacklistForUser(companyId, actorCodeUser(jwt), userId)));
    }

    @PostMapping("/users/{userId}/devices/blacklist")
    @Operation(summary = "Adicionar dispositivo à blacklist de um usuário do tenant")
    @MetricsEndpoint(endpoint = "tenant_add_user_blacklist")
    public ResponseEntity<Void> addUserBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable("userId") String userId,
            @RequestBody DeviceBlacklistRequestDTO request) {

        DeviceBlacklistRequestDTO body = mapper.emptyIfNull(request);
        if (body.getDeviceId() == null || body.getDeviceId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        deviceSessionPort.addDeviceToBlacklistForUser(
                companyId,
                actorCodeUser(jwt),
                userId,
                body.getDeviceId(),
                body.getDeviceName(),
                body.getReason(),
                blockedBy(jwt),
                parseExpiresAt(body.getExpiresAt())
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/devices/blacklist/{deviceId}")
    @Operation(summary = "Remover dispositivo da blacklist de um usuário do tenant")
    @MetricsEndpoint(endpoint = "tenant_remove_user_blacklist")
    public ResponseEntity<Void> removeUserBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable("userId") String userId,
            @PathVariable("deviceId") String deviceId) {

        deviceSessionPort.removeDeviceFromBlacklistForUser(companyId, actorCodeUser(jwt), userId, deviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/devices/blacklist")
    @Operation(summary = "Buscar blacklist do tenant", description = "Busca paginada de dispositivos bloqueados no tenant do ator")
    @MetricsEndpoint(endpoint = "tenant_search_device_blacklist")
    public ResponseEntity<Page<DeviceBlacklistResponseDTO>> searchBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value = "ipAddress", required = false) String ipAddress,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "blockedAt,desc") String sort) {

        return ResponseEntity.ok(mapper.toTenantBlacklistResponsePage(deviceSessionPort.searchTenantBlacklist(
                companyId, actorCodeUser(jwt), userId, deviceId, deviceName, ipAddress, startDate, endDate,
                pageable(page, size, sort)
        )));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Buscar sessões do tenant", description = "Busca paginada de sessões ativas no tenant do ator")
    @MetricsEndpoint(endpoint = "tenant_search_sessions")
    public ResponseEntity<Page<DeviceSessionResponseDTO>> searchSessions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "lastActiveAt,desc") String sort) {

        return ResponseEntity.ok(mapper.toSessionResponsePage(deviceSessionPort.searchTenantSessions(
                companyId, actorCodeUser(jwt), userId, deviceId, pageable(page, size, sort)
        )));
    }

    static String actorCodeUser(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException("Token JWT não informado ou inválido.");
        }
        return jwt.getSubject();
    }

    static String blockedBy(Jwt jwt) {
        if (jwt == null) {
            return "SYSTEM";
        }
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        return jwt.getSubject();
    }

    static LocalDateTime parseExpiresAt(String expiresAtStr) {
        if (expiresAtStr == null || expiresAtStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(expiresAtStr);
        } catch (Exception ignored) {
            return null;
        }
    }

    static Pageable pageable(int page, int size, String sort) {
        int pageSize = Math.min(Math.max(size, 1), 100);
        int pageNumber = Math.max(page, 0);
        String[] sortParams = sort != null ? sort.split(",") : new String[]{"lastActiveAt", "desc"};
        String sortProp = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortProp));
    }
}
