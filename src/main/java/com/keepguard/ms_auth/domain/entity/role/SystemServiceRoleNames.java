package com.keepguard.ms_auth.domain.entity.role;

import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;

import java.util.List;
import java.util.Locale;

public final class SystemServiceRoleNames {

    public static final String ROLE_SERVICE_COLLECTOR = "ROLE_SERVICE_COLLECTOR";
    public static final String ROLE_SERVICE_BFF_CORE = "ROLE_SERVICE_BFF_CORE";
    public static final String ROLE_SERVICE_ANALYST_FINANCE = "ROLE_SERVICE_ANALYST_FINANCE";
    public static final String ROLE_SERVICE_MS_AI_GUARDIAN = "ROLE_SERVICE_MS_AI_GUARDIAN";
    public static final String ROLE_SERVICE_MS_KNOWLEDGE = "ROLE_SERVICE_MS_KNOWLEDGE";
    public static final String ROLE_SERVICE_MS_ANALYST_FINANCE = "ROLE_SERVICE_MS_ANALYST_FINANCE";
    public static final String SERVICE_ROLE_PREFIX = "ROLE_SERVICE_";

    public static final List<String> SERVICE_TEMPLATES = List.of(
            ROLE_SERVICE_COLLECTOR,
            ROLE_SERVICE_BFF_CORE,
            ROLE_SERVICE_ANALYST_FINANCE,
            ROLE_SERVICE_MS_AI_GUARDIAN,
            ROLE_SERVICE_MS_KNOWLEDGE,
            ROLE_SERVICE_MS_ANALYST_FINANCE);

    private SystemServiceRoleNames() {
    }

    public static boolean isServiceRole(String name) {
        return name != null && name.trim().toUpperCase(Locale.ROOT).startsWith(SERVICE_ROLE_PREFIX);
    }

    public static String descriptionFor(String name) {
        if (ROLE_SERVICE_COLLECTOR.equals(name)) {
            return "Perfil para clients OAuth de coleta de dados. Permite leitura e escrita na base de conhecimento.";
        }
        if (ROLE_SERVICE_BFF_CORE.equals(name)) {
            return "Perfil para o BFF Core. Permite leitura na base de conhecimento em nome do usuário autenticado.";
        }
        if (ROLE_SERVICE_ANALYST_FINANCE.equals(name)) {
            return "Perfil para o analista financeiro do investbot. Permite leitura na base de conhecimento e uso do gateway LLM.";
        }
        if (ROLE_SERVICE_MS_AI_GUARDIAN.equals(name)) {
            return "Perfil para o ms-ai-guardian. Permite leitura e escrita no gateway LLM.";
        }
        if (ROLE_SERVICE_MS_KNOWLEDGE.equals(name)) {
            return "Perfil para o ms-knowledge. Permite leitura e escrita no gateway LLM.";
        }
        if (ROLE_SERVICE_MS_ANALYST_FINANCE.equals(name)) {
            return "Perfil para o ms-analyst-finance. Permite leitura na base de conhecimento e uso do gateway LLM.";
        }
        return "Perfil de serviço KeepGuard: " + name;
    }

    public static List<String> authoritiesFor(String roleName) {
        if (ROLE_SERVICE_COLLECTOR.equals(roleName)) {
            return List.of(SystemAuthorityNames.KNOWLEDGE_WRITE, SystemAuthorityNames.KNOWLEDGE_READ);
        }
        if (ROLE_SERVICE_BFF_CORE.equals(roleName)) {
            return List.of(SystemAuthorityNames.KNOWLEDGE_READ);
        }
        if (ROLE_SERVICE_ANALYST_FINANCE.equals(roleName)) {
            return List.of(
                    SystemAuthorityNames.KNOWLEDGE_READ,
                    SystemAuthorityNames.LLM_READ,
                    SystemAuthorityNames.LLM_WRITE);
        }
        if (ROLE_SERVICE_MS_AI_GUARDIAN.equals(roleName)) {
            return List.of(SystemAuthorityNames.LLM_READ, SystemAuthorityNames.LLM_WRITE);
        }
        if (ROLE_SERVICE_MS_KNOWLEDGE.equals(roleName)) {
            return List.of(SystemAuthorityNames.LLM_READ, SystemAuthorityNames.LLM_WRITE);
        }
        if (ROLE_SERVICE_MS_ANALYST_FINANCE.equals(roleName)) {
            return List.of(
                    SystemAuthorityNames.KNOWLEDGE_READ,
                    SystemAuthorityNames.LLM_READ,
                    SystemAuthorityNames.LLM_WRITE);
        }
        return List.of();
    }
}
