package com.keepguard.ms_auth.adapters.in.rest.oauth;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientRuntimeSecretResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.mapper.OAuthClientAdapterMapper;
import com.keepguard.ms_auth.application.port.in.OAuthClientPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/oauth/runtime")
@RequiredArgsConstructor
@Tag(name = "OAuth Runtime", description = "Material cifrado para workers internos (rede core)")
public class OAuthRuntimeSecretController {

    public static final String SECRET_BASE_HEADER = "X-Auth-Client-Secret-Base";

    private final OAuthClientPort oauthClientPort;
    private final OAuthClientAdapterMapper mapper;

    @GetMapping("/secret")
    @Operation(summary = "Obter ciphertext do OAuth client da company")
    @MetricsEndpoint(endpoint = "oauth_client_runtime_secret", operation = "obter ciphertext oauth client")
    public ResponseEntity<OAuthClientRuntimeSecretResponseDTO> getRuntimeSecret(
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(SECRET_BASE_HEADER) String secretBase,
            @RequestParam(required = false) String clientId) {
        return ResponseEntity.ok(mapper.toRuntimeSecretResponse(
                oauthClientPort.findRuntimeSecret(companyId, clientId, secretBase)));
    }
}
