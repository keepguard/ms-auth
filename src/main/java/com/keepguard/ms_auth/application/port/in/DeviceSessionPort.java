package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.auth.AuthLoginViewDTO;
import com.keepguard.ms_auth.application.dto.session.DeviceSessionViewDTO;
import com.keepguard.ms_auth.application.dto.session.PasswordChangedNotifyCommand;
import com.keepguard.ms_auth.application.dto.session.SendDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.dto.session.TenantDeviceBlacklistViewDTO;
import com.keepguard.ms_auth.application.dto.session.VerifyDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DeviceSessionPort {

    Map<String, Object> sendChallenge(SendDeviceChallengeCommandDTO command);

    AuthLoginViewDTO verifyChallenge(VerifyDeviceChallengeCommandDTO command);

    void notifyPasswordChanged(PasswordChangedNotifyCommand command);

    List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId);

    List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId, String requestIp);

    List<DeviceSessionViewDTO> listUserSessions(String codeUser, String currentDeviceId, String requestIp, String requestLocation);

    void revokeSession(String codeUser, String deviceId);

    void revokeAllOtherSessions(String codeUser, String currentDeviceId);

    void revokeAllSessions(String codeUser);

    Map<String, Object> quickRevoke(String token, boolean addToBlacklist);

    void addDeviceToBlacklist(String codeUser, String deviceId, String deviceName, String reason);

    List<DeviceBlacklistEntry> listBlacklist(String codeUser);

    void removeDeviceFromBlacklist(String codeUser, String deviceId);

    Page<DeviceBlacklistEntry> searchBlacklist(
            UUID companyId, UUID codeUser, String deviceId, String deviceName, String ipAddress,
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    void adminAddDeviceToBlacklist(UUID companyId, String codeUser, String deviceId, String deviceName,
                                   String reason, String blockedBy, LocalDateTime expiresAt);

    void adminRemoveDeviceFromBlacklist(UUID companyId, String codeUser, String deviceId);

    List<DeviceSessionViewDTO> listSessionsForUser(
            UUID companyId, String actorCodeUser, String targetCodeUser, String currentDeviceId,
            String requestIp, String requestLocation);

    void revokeSessionForUser(UUID companyId, String actorCodeUser, String targetCodeUser, String deviceId);

    List<TenantDeviceBlacklistViewDTO> listBlacklistForUser(UUID companyId, String actorCodeUser, String targetCodeUser);

    void addDeviceToBlacklistForUser(
            UUID companyId, String actorCodeUser, String targetCodeUser, String deviceId, String deviceName,
            String reason, String blockedBy, LocalDateTime expiresAt);

    void removeDeviceFromBlacklistForUser(UUID companyId, String actorCodeUser, String targetCodeUser, String deviceId);

    Page<TenantDeviceBlacklistViewDTO> searchTenantBlacklist(
            UUID companyId, String actorCodeUser, UUID filterCodeUser, String deviceId, String deviceName,
            String ipAddress, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<DeviceSessionViewDTO> searchTenantSessions(
            UUID companyId, String actorCodeUser, UUID filterCodeUser, String deviceId, Pageable pageable);
}
