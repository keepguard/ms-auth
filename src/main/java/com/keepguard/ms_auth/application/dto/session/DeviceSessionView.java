package com.keepguard.ms_auth.application.dto.session;

import java.util.List;

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
    String createdAt
) {}
