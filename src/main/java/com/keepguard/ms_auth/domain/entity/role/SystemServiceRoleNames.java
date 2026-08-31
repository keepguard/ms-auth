package com.keepguard.ms_auth.domain.entity.role;

import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;

import java.util.List;
import java.util.Locale;

public final class SystemServiceRoleNames {

    public static final String ROLE_SERVICE_COLLECTOR = "ROLE_SERVICE_COLLECTOR";
    public static final String SERVICE_ROLE_PREFIX = "ROLE_SERVICE_";

    public static final List<String> SERVICE_TEMPLATES = List.of(ROLE_SERVICE_COLLECTOR);

    private SystemServiceRoleNames() {
    }

    public static boolean isServiceRole(String name) {
        return name != null && name.trim().toUpperCase(Locale.ROOT).startsWith(SERVICE_ROLE_PREFIX);
    }

    public static String descriptionFor(String name) {
        if (ROLE_SERVICE_COLLECTOR.equals(name)) {
            return "Perfil para clients OAuth de coleta de dados. Permite leitura e escrita na base de conhecimento.";
        }
        return "Perfil de serviço KeepGuard: " + name;
    }

    public static List<String> authoritiesFor(String roleName) {
        if (ROLE_SERVICE_COLLECTOR.equals(roleName)) {
            return List.of(SystemAuthorityNames.KNOWLEDGE_WRITE, SystemAuthorityNames.KNOWLEDGE_READ);
        }
        return List.of();
    }
}
