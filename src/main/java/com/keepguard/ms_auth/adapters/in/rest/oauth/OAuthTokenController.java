package com.keepguard.ms_auth.adapters.in.rest.oauth;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthTokenResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.mapper.OAuthClientAdapterMapper;
import com.keepguard.ms_auth.application.port.in.OAuthClientPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/oauth/token")
@RequiredArgsConstructor
@Tag(name = "OAuth Token", description = "Emissão de JWT de serviço (client credentials)")
public class OAuthTokenController {

    private final OAuthClientPort oauthClientPort;
    private final OAuthClientAdapterMapper mapper;

    @PostMapping
    @Operation(summary = "Emitir token de sistema")
    @MetricsEndpoint(endpoint = "oauth_token", operation = "emitir token oauth")
    public ResponseEntity<OAuthTokenResponseDTO> issueToken(
            @RequestHeader("X-Company-Id") UUID companyId,
            @Valid @RequestBody OAuthTokenRequestDTO request) {
        var view = oauthClientPort.issueToken(mapper.toTokenCommand(request, companyId));
        return ResponseEntity.ok(mapper.toTokenResponse(view));
    }
}
