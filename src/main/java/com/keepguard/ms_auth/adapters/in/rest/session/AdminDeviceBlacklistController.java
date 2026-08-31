package com.keepguard.ms_auth.adapters.in.rest.session;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_auth.application.dto.session.TenantDeviceBlacklistView;
import com.keepguard.ms_auth.application.service.session.DeviceSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/devices/blacklist")
@RequiredArgsConstructor
@Tag(name = "Admin Device Blacklist", description = "Alias depreciado. Use /api/v1/devices/blacklist e /api/v1/users/{userId}/devices/blacklist")
public class AdminDeviceBlacklistController {

    private final DeviceSessionService deviceSessionService;

    @GetMapping
    @Deprecated
    @Operation(summary = "Consultar blacklist do tenant (depreciado)",
               description = "Use GET /api/v1/devices/blacklist")
    @MetricsEndpoint(endpoint = "admin_search_device_blacklist")
    public ResponseEntity<Page<TenantDeviceBlacklistView>> searchBlacklist(
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

        Page<TenantDeviceBlacklistView> result = deviceSessionService.searchTenantBlacklist(
                companyId,
                TenantDeviceController.actorCodeUser(jwt),
                userId,
                deviceId,
                deviceName,
                ipAddress,
                startDate,
                endDate,
                TenantDeviceController.pageable(page, size, sort)
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Deprecated
    @Operation(summary = "Bloquear dispositivo (depreciado)",
               description = "Use POST /api/v1/users/{userId}/devices/blacklist")
    @MetricsEndpoint(endpoint = "admin_add_device_blacklist")
    public ResponseEntity<Void> addDeviceToBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestBody Map<String, Object> request) {

        String userId = request != null ? stringValue(request.get("userId")) : null;
        String deviceId = request != null ? stringValue(request.get("deviceId")) : null;
        if (userId == null || userId.isBlank() || deviceId == null || deviceId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        deviceSessionService.addDeviceToBlacklistForUser(
                companyId,
                TenantDeviceController.actorCodeUser(jwt),
                userId,
                deviceId,
                stringValue(request.get("deviceName")),
                stringValue(request.get("reason")),
                TenantDeviceController.blockedBy(jwt),
                TenantDeviceController.parseExpiresAt(stringValue(request.get("expiresAt")))
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{deviceId}")
    @Deprecated
    @Operation(summary = "Desbloquear dispositivo (depreciado)",
               description = "Use DELETE /api/v1/users/{userId}/devices/blacklist/{deviceId}")
    @MetricsEndpoint(endpoint = "admin_remove_device_blacklist")
    public ResponseEntity<Void> removeDeviceFromBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable("deviceId") String deviceId,
            @RequestParam("userId") String userId) {

        if (userId == null || userId.isBlank() || deviceId == null || deviceId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        deviceSessionService.removeDeviceFromBlacklistForUser(
                companyId, TenantDeviceController.actorCodeUser(jwt), userId, deviceId);
        return ResponseEntity.noContent().build();
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
