package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import com.keepguard.ms_auth.infrastructure.persistence.entity.OAuthClientJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OAuthClientJpaMapper {

    public OAuthClientJpaEntity toJpaEntity(OAuthClient domain) {
        if (domain == null) {
            return null;
        }
        return OAuthClientJpaEntity.builder()
                .id(domain.getId())
                .companyId(domain.getCompanyId())
                .clientId(domain.getClientId())
                .secretHash(domain.getSecretHash())
                .secretEncrypted(domain.getSecretEncrypted())
                .serviceRoleId(domain.getServiceRoleId())
                .authorities(domain.getServiceRoleId() != null ? new ArrayList<>() : copyAuthorities(domain.getAuthorities()))
                .status(domain.getStatus())
                .tokenTtlSeconds(domain.getTokenTtlSeconds())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public OAuthClient toDomain(OAuthClientJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return OAuthClient.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .clientId(entity.getClientId())
                .secretHash(entity.getSecretHash())
                .secretEncrypted(entity.getSecretEncrypted())
                .serviceRoleId(entity.getServiceRoleId())
                .authorities(copyAuthorities(entity.getAuthorities()))
                .status(entity.getStatus())
                .tokenTtlSeconds(entity.getTokenTtlSeconds())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private List<String> copyAuthorities(List<String> authorities) {
        return authorities == null ? new ArrayList<>() : new ArrayList<>(authorities);
    }
}
