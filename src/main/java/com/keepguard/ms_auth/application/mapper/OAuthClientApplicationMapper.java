package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OAuthClientApplicationMapper {

    public OAuthClientCreateView toCreateView(OAuthClient client, String plainSecret) {
        if (client == null) {
            return null;
        }
        return new OAuthClientCreateView(
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

    public OAuthClientView toView(OAuthClient client) {
        if (client == null) {
            return null;
        }
        return new OAuthClientView(
                client.getId(),
                client.getCompanyId(),
                client.getClientId(),
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
