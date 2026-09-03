package com.keepguard.ms_auth.adapters.in.rest.oauth.dto;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ciphertext do OAuth client para workers internos")
public class OAuthClientRuntimeSecretResponseDTO {
    private String clientId;
    private String secretEncrypted;
    private OAuthClientStatus status;
}
