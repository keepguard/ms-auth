package com.keepguard.ms_auth.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterLoginCommandDTO {
    
    private String username;
    private String passwordHash;
    private UUID xApplicationUuid;
    private String userAgent;
}
