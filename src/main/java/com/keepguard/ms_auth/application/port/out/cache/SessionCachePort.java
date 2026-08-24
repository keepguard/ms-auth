package com.keepguard.ms_auth.application.port.out.cache;

import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;
import com.keepguard.ms_auth.domain.entity.session.DeviceChallengeSession;
import com.keepguard.ms_auth.domain.entity.session.QuickRevokeToken;
import com.keepguard.ms_auth.domain.entity.session.UserSession;

import java.util.List;
import java.util.Optional;

public interface SessionCachePort {

    void saveUserSession(UserSession session, long ttlSeconds);

    Optional<UserSession> getUserSession(String codeUser, String deviceId);

    List<UserSession> listUserSessions(String codeUser);

    void removeUserSession(String codeUser, String deviceId);

    void removeAllUserSessionsExceptCurrent(String codeUser, String currentDeviceId);

    void removeAllUserSessions(String codeUser);

    void saveDeviceChallenge(DeviceChallengeSession challenge, long ttlSeconds);

    Optional<DeviceChallengeSession> getDeviceChallenge(String challengeSessionId);

    void removeDeviceChallenge(String challengeSessionId);

    void addToBlacklist(DeviceBlacklistEntry entry, long ttlSeconds);

    boolean isDeviceBlacklisted(String codeUser, String deviceId);

    List<DeviceBlacklistEntry> listBlacklistedDevices(String codeUser);

    void removeFromBlacklist(String codeUser, String deviceId);

    void saveQuickRevokeToken(QuickRevokeToken quickRevokeToken, long ttlSeconds);

    Optional<QuickRevokeToken> getQuickRevokeToken(String token);

    void removeQuickRevokeToken(String token);
}
