package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientViewDTO;
import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OAuthClientApplicationMapper {

    public OAuthClientCreateViewDTO toCreateView(OAuthClient client, String plainSecret) {
        if (client == null) {
            return null;
        }
        return new OAuthClientCreateViewDTO(
                client.getId(),
                client.getCompanyId(),
                client.getClientId(),
                plainSecret,
                client.getServiceRoleId(),
                client.getServiceRoleName(),
                copy(client.getAuthorities()),
                client.getStatus(),
                client.getTokenTtlSeconds(),
                client.getDescription(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }

    public OAuthClientViewDTO toView(OAuthClient client) {
        return toView(client, null);
    }

    public OAuthClientViewDTO toView(OAuthClient client, String clientSecret) {
        if (client == null) {
            return null;
        }
        return new OAuthClientViewDTO(
                client.getId(),
                client.getCompanyId(),
                client.getClientId(),
                clientSecret,
                client.getServiceRoleId(),
                client.getServiceRoleName(),
                copy(client.getAuthorities()),
                client.getStatus(),
                client.getTokenTtlSeconds(),
                client.getDescription(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }

    private List<String> copy(List<String> authorities) {
        return authorities == null ? List.of() : new ArrayList<>(authorities);
    }
}
