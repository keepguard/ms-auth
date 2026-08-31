package com.keepguard.ms_auth.adapters.in.rest.oauth.mapper;

import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientCreateRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientCreateResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthTokenResponseDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenView;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientCreateCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientIdCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthTokenCommandDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OAuthClientAdapterMapper {

    public OAuthClientCreateCommandDTO toCreateCommand(OAuthClientCreateRequestDTO dto, UUID companyId) {
        if (dto == null) {
            return null;
        }
        return OAuthClientCreateCommandDTO.builder()
                .companyId(companyId)
                .clientId(dto.getClientId())
                .description(dto.getDescription())
                .authorities(dto.getAuthorities())
                .tokenTtlSeconds(dto.getTokenTtlSeconds())
                .build();
    }

    public OAuthClientIdCommandDTO toIdCommand(UUID companyId, UUID id) {
        return OAuthClientIdCommandDTO.builder()
                .companyId(companyId)
                .id(id)
                .build();
    }

    public OAuthTokenCommandDTO toTokenCommand(OAuthTokenRequestDTO dto, UUID companyId) {
        if (dto == null) {
            return null;
        }
        return OAuthTokenCommandDTO.builder()
                .companyId(companyId)
                .grantType(dto.getGrantType())
                .clientId(dto.getClientId())
                .clientSecret(dto.getClientSecret())
                .agentId(dto.getAgentId())
                .agentCode(dto.getAgentCode())
                .build();
    }

    public OAuthClientCreateResponseDTO toCreateResponse(OAuthClientCreateView view) {
        if (view == null) {
            return null;
        }
        return OAuthClientCreateResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .clientId(view.clientId())
                .clientSecret(view.clientSecret())
                .authorities(view.authorities())
                .status(view.status())
                .tokenTtlSeconds(view.tokenTtlSeconds())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public OAuthClientResponseDTO toResponse(OAuthClientView view) {
        if (view == null) {
            return null;
        }
        return OAuthClientResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .clientId(view.clientId())
                .authorities(view.authorities())
                .status(view.status())
                .tokenTtlSeconds(view.tokenTtlSeconds())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public OAuthTokenResponseDTO toTokenResponse(OAuthTokenView view) {
        if (view == null) {
            return null;
        }
        return OAuthTokenResponseDTO.builder()
                .accessToken(view.accessToken())
                .tokenType(view.tokenType())
                .expiresIn(view.expiresIn())
                .build();
    }
}
