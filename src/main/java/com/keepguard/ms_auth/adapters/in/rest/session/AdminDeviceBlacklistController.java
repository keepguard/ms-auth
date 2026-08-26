package com.keepguard.ms_auth.adapters.in.rest.session;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.lib_common.utils.ValidationUtils;
import com.keepguard.ms_auth.application.service.session.DeviceSessionService;
import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Admin Device Blacklist", description = "Endpoints administrativos para auditoria e gestão da blacklist de dispositivos")
public class AdminDeviceBlacklistController {

    private final DeviceSessionService deviceSessionService;

    @GetMapping
    @Operation(summary = "Consultar e filtrar blacklist de dispositivos",
               description = "Permite a administradores e gestores pesquisar o histórico de dispositivos bloqueados no Tenant com filtros dinâmicos e paginação")
    @MetricsEndpoint(endpoint = "admin_search_device_blacklist")
    public ResponseEntity<Page<DeviceBlacklistEntry>> searchBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Tenant-Id") String tenantIdHeader,
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value = "ipAddress", required = false) String ipAddress,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "blockedAt,desc") String sort) {

        validateAdminOrManagerRole(jwt);
        UUID tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        // Limita o tamanho máximo da página a 100 registros (boas práticas)
        int pageSize = Math.min(Math.max(size, 1), 100);
        int pageNumber = Math.max(page, 0);

        String[] sortParams = sort.split(",");
        String sortProp = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortProp));

        Page<DeviceBlacklistEntry> result = deviceSessionService.searchBlacklist(
                tenantId, userId, deviceId, deviceName, ipAddress, startDate, endDate, pageable
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Bloquear dispositivo administrativamente",
               description = "Permite bloquear o dispositivo de um usuário específico, encerrando suas sessões ativas")
    @MetricsEndpoint(endpoint = "admin_add_device_blacklist")
    public ResponseEntity<Void> addDeviceToBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Tenant-Id") String tenantIdHeader,
            @RequestBody Map<String, Object> request) {

        validateAdminOrManagerRole(jwt);
        UUID tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        String blockedBy = jwt != null && jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : (jwt != null ? jwt.getSubject() : "ADMIN");

        String userId = (String) request.get("userId");
        String deviceId = (String) request.get("deviceId");
        String deviceName = (String) request.get("deviceName");
        String reason = (String) request.get("reason");
        String expiresAtStr = (String) request.get("expiresAt");

        LocalDateTime expiresAt = null;
        if (expiresAtStr != null && !expiresAtStr.isBlank()) {
            try {
                expiresAt = LocalDateTime.parse(expiresAtStr);
            } catch (Exception ignored) {}
        }

        if (userId == null || userId.isBlank() || deviceId == null || deviceId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        deviceSessionService.adminAddDeviceToBlacklist(tenantId, userId, deviceId, deviceName, reason, blockedBy, expiresAt);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Desbloquear dispositivo administrativamente",
               description = "Remove o dispositivo da blacklist de determinado usuário")
    @MetricsEndpoint(endpoint = "admin_remove_device_blacklist")
    public ResponseEntity<Void> removeDeviceFromBlacklist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Tenant-Id") String tenantIdHeader,
            @PathVariable("deviceId") String deviceId,
            @RequestParam("userId") String userId) {

        validateAdminOrManagerRole(jwt);
        UUID tenantId = ValidationUtils.validateTenantId(tenantIdHeader);

        if (userId == null || userId.isBlank() || deviceId == null || deviceId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        deviceSessionService.adminRemoveDeviceFromBlacklist(tenantId, userId, deviceId);
        return ResponseEntity.noContent().build();
    }

    private void validateAdminOrManagerRole(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Token JWT não informado ou inválido.");
        }
        java.util.List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null || roles.isEmpty()) {
            String roleSingle = jwt.getClaimAsString("roles");
            if (roleSingle != null) {
                roles = java.util.List.of(roleSingle);
            }
        }
        if (roles == null || roles.stream().noneMatch(r -> r.equalsIgnoreCase("ROLE_ADMIN") || r.equalsIgnoreCase("ROLE_MANAGER") || r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("MANAGER"))) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso restrito a administradores e gestores.");
        }
    }
}
