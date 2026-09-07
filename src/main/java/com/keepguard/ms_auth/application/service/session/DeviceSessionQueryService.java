package com.keepguard.ms_auth.application.service.session;

import com.keepguard.ms_auth.application.dto.session.DeviceSessionViewDTO;
import com.keepguard.ms_auth.application.dto.session.TenantDeviceBlacklistViewDTO;
import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceSessionQueryService {

    private final DeviceSessionCommandService commandService;

    public List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId) {
        return commandService.listUserSessions(codeUser, currentDeviceId);
    }

    public List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId, String requestIp) {
        return commandService.listUserSessions(codeUser, currentDeviceId, requestIp);
    }

    public List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId, String requestIp, String requestLocation) {
        return commandService.listUserSessions(codeUser, currentDeviceId, requestIp, requestLocation);
    }

    public List<DeviceBlacklistEntry> listBlacklist(String codeUser) {
        return commandService.listBlacklist(codeUser);
    }

    public Page<DeviceBlacklistEntry> searchBlacklist(
            UUID companyId, UUID codeUser, String deviceId, String deviceName, String ipAddress,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return commandService.searchBlacklist(companyId, codeUser, deviceId, deviceName, ipAddress, from, to, pageable);
    }

    public List<DeviceSessionViewDTO> listSessionsForUser(
            UUID companyId, String actorCodeUser, String targetCodeUser, String currentDeviceId,
            String requestIp, String requestLocation) {
        return commandService.listSessionsForUser(companyId, actorCodeUser, targetCodeUser, currentDeviceId, requestIp, requestLocation);
    }

    public List<TenantDeviceBlacklistViewDTO> listBlacklistForUser(UUID companyId, String actorCodeUser, String targetCodeUser) {
        return commandService.listBlacklistForUser(companyId, actorCodeUser, targetCodeUser);
    }

    public Page<TenantDeviceBlacklistViewDTO> searchTenantBlacklist(
            UUID companyId, String actorCodeUser, UUID filterCodeUser, String deviceId, String deviceName,
            String ipAddress, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return commandService.searchTenantBlacklist(companyId, actorCodeUser, filterCodeUser, deviceId, deviceName, ipAddress, from, to, pageable);
    }

    public Page<DeviceSessionViewDTO> searchTenantSessions(
            UUID companyId, String actorCodeUser, UUID filterCodeUser, String deviceId, Pageable pageable) {
        return commandService.searchTenantSessions(companyId, actorCodeUser, filterCodeUser, deviceId, pageable);
    }
}
