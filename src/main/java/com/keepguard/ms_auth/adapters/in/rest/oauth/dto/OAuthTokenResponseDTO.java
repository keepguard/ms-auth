package com.keepguard.ms_auth.adapters.in.rest.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token de serviço")
public class OAuthTokenResponseDTO {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
