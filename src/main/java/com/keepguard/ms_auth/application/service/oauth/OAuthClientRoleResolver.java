package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemServiceRoleNames;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthClientRoleResolver {

    private final RoleRepositoryPort roleRepository;

    public record Resolved(UUID roleId, String roleName, List<String> authorities) {
    }

    public Role requireAssignable(UUID roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("roleId é obrigatório.");
        }
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Service role não encontrada."));
        if (role.getCompanyId() != null || !SystemServiceRoleNames.isServiceRole(role.getName())) {
            throw new IllegalArgumentException("roleId deve ser uma service role global.");
        }
        return role;
    }

    public Resolved resolve(OAuthClient client) {
        if (client == null) {
            return new Resolved(null, null, List.of());
        }
        if (client.getServiceRoleId() != null) {
            return roleRepository.findById(client.getServiceRoleId())
                    .filter(role -> role.getCompanyId() == null)
                    .filter(role -> SystemServiceRoleNames.isServiceRole(role.getName()))
                    .map(role -> new Resolved(role.getId(), role.getName(), flatten(role)))
                    .orElseGet(() -> fallback(client));
        }
        return fallback(client);
    }

    public OAuthClient enrich(OAuthClient client) {
        Resolved resolved = resolve(client);
        client.setServiceRoleId(resolved.roleId());
        client.setServiceRoleName(resolved.roleName());
        client.setAuthorities(new ArrayList<>(resolved.authorities()));
        return client;
    }

    private List<String> flatten(Role role) {
        if (role.getAuthorities() == null || role.getAuthorities().isEmpty()) {
            return List.of();
        }
        return role.getAuthorities().stream()
                .map(Authority::getName)
                .distinct()
                .sorted()
                .toList();
    }

    private Resolved fallback(OAuthClient client) {
        List<String> authorities = client.getAuthorities() == null
                ? List.of()
                : List.copyOf(client.getAuthorities());
        return new Resolved(client.getServiceRoleId(), client.getServiceRoleName(), authorities);
    }
}
