package com.keepguard.ms_auth.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuedTokenPairDTO {
    private String accessToken;
    private String refreshToken;
    private String sid;
    private Long expiresIn;
}
