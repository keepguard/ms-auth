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

    public static final List<String> USER_ACTIONS = List.of(USER_BLOCK, USER_UNBLOCK, USER_DELETE);
    public static final List<String> MANAGER_ACTIONS = List.of(MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE);
    public static final List<String> TEMPLATES = List.of(
            USER_BLOCK, USER_UNBLOCK, USER_DELETE,
            MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE,
            AUDIT_READ,
            KNOWLEDGE_WRITE,
            KNOWLEDGE_READ
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
        return "Permissão do catálogo KeepGuard: " + name;
    }

    public static List<String> defaultAuthoritiesForRole(String roleName) {
        if (SystemRoleNames.ROLE_ADMIN.equals(roleName)) {
            return List.of(
                    USER_BLOCK, USER_UNBLOCK, USER_DELETE,
                    MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE,
                    AUDIT_READ
            );
        }
        if (SystemRoleNames.ROLE_MANAGER.equals(roleName)) {
            return USER_ACTIONS;
        }
        return List.of();
    }
}
