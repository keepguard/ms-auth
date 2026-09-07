package com.keepguard.ms_auth.adapters.in.rest.oauth.mapper;

import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientCreateRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientCreateResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientRuntimeSecretResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientUpdateRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthServiceRoleAuthorityDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthServiceRoleResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthTokenResponseDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientRuntimeSecretViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthServiceRoleViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientIdCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientUpdateCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenCommandDTO;
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
                .roleId(dto.getRoleId())
                .tokenTtlSeconds(dto.getTokenTtlSeconds())
                .build();
    }

    public OAuthClientUpdateCommandDTO toUpdateCommand(OAuthClientUpdateRequestDTO dto, UUID companyId, UUID id) {
        if (dto == null) {
            return null;
        }
        return OAuthClientUpdateCommandDTO.builder()
                .companyId(companyId)
                .id(id)
                .description(dto.getDescription())
                .roleId(dto.getRoleId())
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

    public OAuthClientCreateResponseDTO toCreateResponse(OAuthClientCreateViewDTO view) {
        if (view == null) {
            return null;
        }
        return OAuthClientCreateResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .clientId(view.clientId())
                .clientSecret(view.clientSecret())
                .serviceRoleId(view.serviceRoleId())
                .serviceRoleName(view.serviceRoleName())
                .authorities(view.authorities())
                .status(view.status())
                .tokenTtlSeconds(view.tokenTtlSeconds())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public OAuthClientResponseDTO toResponse(OAuthClientViewDTO view) {
        if (view == null) {
            return null;
        }
        return OAuthClientResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .clientId(view.clientId())
                .clientSecret(view.clientSecret())
                .serviceRoleId(view.serviceRoleId())
                .serviceRoleName(view.serviceRoleName())
                .authorities(view.authorities())
                .status(view.status())
                .tokenTtlSeconds(view.tokenTtlSeconds())
                .description(view.description())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public OAuthTokenResponseDTO toTokenResponse(OAuthTokenViewDTO view) {
        if (view == null) {
            return null;
        }
        return OAuthTokenResponseDTO.builder()
                .accessToken(view.accessToken())
                .tokenType(view.tokenType())
                .expiresIn(view.expiresIn())
                .build();
    }

    public OAuthServiceRoleResponseDTO toServiceRoleResponse(OAuthServiceRoleViewDTO view) {
        if (view == null) {
            return null;
        }
        return OAuthServiceRoleResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .description(view.description())
                .authorities(view.authorities() == null ? java.util.List.of() : view.authorities().stream()
                        .map(item -> OAuthServiceRoleAuthorityDTO.builder()
                                .name(item.name())
                                .description(item.description())
                                .build())
                        .toList())
                .build();
    }

    public OAuthClientRuntimeSecretResponseDTO toRuntimeSecretResponse(OAuthClientRuntimeSecretViewDTO view) {
        if (view == null) {
            return null;
        }
        return OAuthClientRuntimeSecretResponseDTO.builder()
                .clientId(view.clientId())
                .secretEncrypted(view.secretEncrypted())
                .status(view.status())
                .build();
    }
}
