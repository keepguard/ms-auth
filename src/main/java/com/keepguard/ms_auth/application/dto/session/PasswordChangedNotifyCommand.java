package com.keepguard.ms_auth.application.dto.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangedNotifyCommand {

    private String codeUser;
    private String companyId;
    private String email;
    private String username;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String userAgent;
}
