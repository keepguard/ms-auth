package com.keepguard.ms_auth.application.service.session;

import com.keepguard.ms_auth.application.dto.auth.AuthLoginViewDTO;
import com.keepguard.ms_auth.application.dto.session.DeviceSessionViewDTO;
import com.keepguard.ms_auth.application.dto.session.PasswordChangedNotifyCommand;
import com.keepguard.ms_auth.application.dto.session.SendDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.dto.session.TenantDeviceBlacklistViewDTO;
import com.keepguard.ms_auth.application.dto.session.VerifyDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.port.in.DeviceSessionPort;
import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceSessionUseCaseService implements DeviceSessionPort {

    private final DeviceSessionCommandService commandService;
    private final DeviceSessionQueryService queryService;

    @Override
    public Map<String, Object> sendChallenge(SendDeviceChallengeCommandDTO command) {
        return commandService.sendChallenge(command);
    }

    @Override
    public AuthLoginViewDTO verifyChallenge(VerifyDeviceChallengeCommandDTO command) {
        return commandService.verifyChallenge(command);
    }

    @Override
    public void notifyPasswordChanged(PasswordChangedNotifyCommand command) {
        commandService.notifyPasswordChanged(command);
    }

    @Override
    public List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId) {
        return queryService.listUserSessions(codeUser, currentDeviceId);
    }

    @Override
    public List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId, String requestIp) {
        return queryService.listUserSessions(codeUser, currentDeviceId, requestIp);
    }

    @Override
    public List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId, String requestIp, String requestLocation) {
        return queryService.listUserSessions(codeUser, currentDeviceId, requestIp, requestLocation);
    }

    @Override
    public void revokeSession(String codeUser, String deviceId) {
        commandService.revokeSession(codeUser, deviceId);
    }

    @Override
    public void revokeAllOtherSessions(String codeUser, String currentDeviceId) {
        commandService.revokeAllOtherSessions(codeUser, currentDeviceId);
    }

    @Override
    public void revokeAllSessions(String codeUser) {
        commandService.revokeAllSessions(codeUser);
    }

    @Override
    public Map<String, Object> quickRevoke(String token, boolean addToBlacklist) {
        return commandService.quickRevoke(token, addToBlacklist);
    }

    @Override
    public void addDeviceToBlacklist(String codeUser, String deviceId, String deviceName, String reason) {
        commandService.addDeviceToBlacklist(codeUser, deviceId, deviceName, reason);
    }

    @Override
    public List<DeviceBlacklistEntry> listBlacklist(String codeUser) {
        return queryService.listBlacklist(codeUser);
    }

    @Override
    public void removeDeviceFromBlacklist(String codeUser, String deviceId) {
        commandService.removeDeviceFromBlacklist(codeUser, deviceId);
    }

    @Override
    public Page<DeviceBlacklistEntry> searchBlacklist(
            UUID companyId, UUID codeUser, String deviceId, String deviceName, String ipAddress,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return queryService.searchBlacklist(companyId, codeUser, deviceId, deviceName, ipAddress, from, to, pageable);
    }

    @Override
    public void adminAddDeviceToBlacklist(UUID companyId, String codeUser, String deviceId, String deviceName,
                                          String reason, String blockedBy, LocalDateTime expiresAt) {
        commandService.adminAddDeviceToBlacklist(companyId, codeUser, deviceId, deviceName, reason, blockedBy, expiresAt);
    }

    @Override
    public void adminRemoveDeviceFromBlacklist(UUID companyId, String codeUser, String deviceId) {
        commandService.adminRemoveDeviceFromBlacklist(companyId, codeUser, deviceId);
    }

    @Override
    public List<DeviceSessionViewDTO> listSessionsForUser(
            UUID companyId, String actorCodeUser, String targetCodeUser, String currentDeviceId,
            String requestIp, String requestLocation) {
        return queryService.listSessionsForUser(companyId, actorCodeUser, targetCodeUser, currentDeviceId, requestIp, requestLocation);
    }

    @Override
    public void revokeSessionForUser(UUID companyId, String actorCodeUser, String targetCodeUser, String deviceId) {
        commandService.revokeSessionForUser(companyId, actorCodeUser, targetCodeUser, deviceId);
    }

    @Override
    public List<TenantDeviceBlacklistViewDTO> listBlacklistForUser(UUID companyId, String actorCodeUser, String targetCodeUser) {
        return queryService.listBlacklistForUser(companyId, actorCodeUser, targetCodeUser);
    }

    @Override
    public void addDeviceToBlacklistForUser(
            UUID companyId, String actorCodeUser, String targetCodeUser, String deviceId, String deviceName,
            String reason, String blockedBy, LocalDateTime expiresAt) {
        commandService.addDeviceToBlacklistForUser(companyId, actorCodeUser, targetCodeUser, deviceId, deviceName, reason, blockedBy, expiresAt);
    }

    @Override
    public void removeDeviceFromBlacklistForUser(UUID companyId, String actorCodeUser, String targetCodeUser, String deviceId) {
        commandService.removeDeviceFromBlacklistForUser(companyId, actorCodeUser, targetCodeUser, deviceId);
    }

    @Override
    public Page<TenantDeviceBlacklistViewDTO> searchTenantBlacklist(
            UUID companyId, String actorCodeUser, UUID filterCodeUser, String deviceId, String deviceName,
            String ipAddress, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return queryService.searchTenantBlacklist(companyId, actorCodeUser, filterCodeUser, deviceId, deviceName, ipAddress, from, to, pageable);
    }

    @Override
    public Page<DeviceSessionViewDTO> searchTenantSessions(
            UUID companyId, String actorCodeUser, UUID filterCodeUser, String deviceId, Pageable pageable) {
        return queryService.searchTenantSessions(companyId, actorCodeUser, filterCodeUser, deviceId, pageable);
    }
}
