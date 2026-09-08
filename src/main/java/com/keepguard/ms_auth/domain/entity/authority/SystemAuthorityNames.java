package com.keepguard.ms_auth.domain.entity.authority;

import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;

import java.util.List;

public final class SystemAuthorityNames {

    public static final String USER_BLOCK = "user:block";
    public static final String USER_UNBLOCK = "user:unblock";
    public static final String USER_DELETE = "user:delete";
    public static final String MANAGER_BLOCK = "manager:block";
    public static final String MANAGER_UNBLOCK = "manager:unblock";
    public static final String MANAGER_DELETE = "manager:delete";
    public static final String AUDIT_READ = "audit:read";
    public static final String KNOWLEDGE_WRITE = "knowledge:write";
    public static final String KNOWLEDGE_READ = "knowledge:read";
    public static final String LLM_READ = "llm:read";
    public static final String LLM_WRITE = "llm:write";
    public static final String COLLECTOR_READ = "collector:read";
    public static final String COLLECTOR_WRITE = "collector:write";
    public static final String GUARDIAN_READ = "guardian:read";
    public static final String GUARDIAN_WRITE = "guardian:write";
    public static final String OAUTH_READ = "oauth:read";
    public static final String OAUTH_WRITE = "oauth:write";
    public static final String SESSION_READ = "session:read";
    public static final String SESSION_WRITE = "session:write";
    public static final String OPS_READ = "ops:read";
    public static final String BILLING_READ = "billing:read";
    public static final String BILLING_WRITE = "billing:write";

    public static final List<String> USER_ACTIONS = List.of(USER_BLOCK, USER_UNBLOCK, USER_DELETE);
    public static final List<String> MANAGER_ACTIONS = List.of(MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE);
    public static final List<String> TEMPLATES = List.of(
            USER_BLOCK, USER_UNBLOCK, USER_DELETE,
            MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE,
            AUDIT_READ,
            KNOWLEDGE_WRITE,
            KNOWLEDGE_READ,
            LLM_READ,
            LLM_WRITE,
            COLLECTOR_READ,
            COLLECTOR_WRITE,
            GUARDIAN_READ,
            GUARDIAN_WRITE,
            OAUTH_READ,
            OAUTH_WRITE,
            SESSION_READ,
            SESSION_WRITE,
            OPS_READ,
            BILLING_READ,
            BILLING_WRITE
    );

    private SystemAuthorityNames() {
    }

    public static String descriptionFor(String name) {
        if (USER_BLOCK.equals(name)) {
            return "Permite bloquear a conta de um usuário";
        }
        if (USER_UNBLOCK.equals(name)) {
            return "Permite desbloquear a conta de um usuário";
        }
        if (USER_DELETE.equals(name)) {
            return "Permite excluir a conta de um usuário";
        }
        if (MANAGER_BLOCK.equals(name)) {
            return "Permite bloquear a conta de um gerente";
        }
        if (MANAGER_UNBLOCK.equals(name)) {
            return "Permite desbloquear a conta de um gerente";
        }
        if (MANAGER_DELETE.equals(name)) {
            return "Permite excluir a conta de um gerente";
        }
        if (AUDIT_READ.equals(name)) {
            return "Permite consultar eventos de auditoria";
        }
        if (KNOWLEDGE_WRITE.equals(name)) {
            return "Permite gravar documentos na base de conhecimento da empresa";
        }
        if (KNOWLEDGE_READ.equals(name)) {
            return "Permite consultar documentos na base de conhecimento da empresa";
        }
        if (LLM_READ.equals(name)) {
            return "Permite consultar uso, provedores e alertas LLM";
        }
        if (LLM_WRITE.equals(name)) {
            return "Permite gerenciar provedores e regras de alerta LLM";
        }
        if (COLLECTOR_READ.equals(name)) {
            return "Permite consultar agents, execuções, fontes e incidentes de coleta";
        }
        if (COLLECTOR_WRITE.equals(name)) {
            return "Permite criar, alterar e operar agents, fontes e incidentes de coleta";
        }
        if (GUARDIAN_READ.equals(name)) {
            return "Permite consultar incidentes e destinatários do Guardian";
        }
        if (GUARDIAN_WRITE.equals(name)) {
            return "Permite executar ações no Guardian e gerenciar destinatários";
        }
        if (OAUTH_READ.equals(name)) {
            return "Permite consultar clients OAuth da empresa";
        }
        if (OAUTH_WRITE.equals(name)) {
            return "Permite criar, alterar, bloquear e excluir clients OAuth";
        }
        if (SESSION_READ.equals(name)) {
            return "Permite consultar sessões e blacklist da organização";
        }
        if (SESSION_WRITE.equals(name)) {
            return "Permite revogar sessões e alterar a blacklist da organização";
        }
        if (OPS_READ.equals(name)) {
            return "Permite consultar o health das conexões do core";
        }
        if (BILLING_READ.equals(name)) {
            return "Permite consultar planos, faturas e a própria assinatura";
        }
        if (BILLING_WRITE.equals(name)) {
            return "Permite cadastrar credencial Asaas e CRUD de planos da company";
        }
        return "Permissão do catálogo KeepGuard: " + name;
    }

    public static List<String> defaultAuthoritiesForRole(String roleName) {
        if (SystemRoleNames.ROLE_ADMIN.equals(roleName)) {
            return List.of(
                    USER_BLOCK, USER_UNBLOCK, USER_DELETE,
                    MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE,
                    AUDIT_READ, LLM_READ, LLM_WRITE,
                    COLLECTOR_READ, COLLECTOR_WRITE,
                    GUARDIAN_READ, GUARDIAN_WRITE,
                    OAUTH_READ, OAUTH_WRITE,
                    SESSION_READ, SESSION_WRITE,
                    OPS_READ,
                    BILLING_READ, BILLING_WRITE
            );
        }
        if (SystemRoleNames.ROLE_MANAGER.equals(roleName)) {
            return List.of(
                    USER_BLOCK, USER_UNBLOCK, USER_DELETE,
                    LLM_READ,
                    COLLECTOR_READ,
                    SESSION_READ, SESSION_WRITE,
                    BILLING_READ
            );
        }
        if (SystemRoleNames.ROLE_USER.equals(roleName)) {
            return List.of(BILLING_READ);
        }
        return List.of();
    }
}
