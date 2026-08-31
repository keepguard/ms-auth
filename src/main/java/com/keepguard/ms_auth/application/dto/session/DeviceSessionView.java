package com.keepguard.ms_auth.application.dto.session;

public record DeviceSessionView(
    String sessionId,
    String deviceId,
    String deviceName,
    String deviceType,
    String ipAddress,
    String location,
    Boolean isCurrent,
    Boolean isTrusted,
    String lastActiveAt,
    String createdAt,
    String codeUser,
    Boolean writable
) {
    public DeviceSessionView(
            String sessionId,
            String deviceId,
            String deviceName,
            String deviceType,
            String ipAddress,
            String location,
            Boolean isCurrent,
            Boolean isTrusted,
            String lastActiveAt,
            String createdAt) {
        this(sessionId, deviceId, deviceName, deviceType, ipAddress, location, isCurrent, isTrusted, lastActiveAt, createdAt, null, null);
    }
}
