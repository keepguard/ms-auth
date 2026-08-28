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

    public static final List<String> USER_ACTIONS = List.of(USER_BLOCK, USER_UNBLOCK, USER_DELETE);
    public static final List<String> MANAGER_ACTIONS = List.of(MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE);
    public static final List<String> TEMPLATES = List.of(
            USER_BLOCK, USER_UNBLOCK, USER_DELETE,
            MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE
    );

    private SystemAuthorityNames() {
    }

    public static List<String> defaultAuthoritiesForRole(String roleName) {
        if (SystemRoleNames.ROLE_ADMIN.equals(roleName)) {
            return List.of(
                    USER_BLOCK, USER_UNBLOCK, USER_DELETE,
                    MANAGER_BLOCK, MANAGER_UNBLOCK, MANAGER_DELETE
            );
        }
        if (SystemRoleNames.ROLE_MANAGER.equals(roleName)) {
            return USER_ACTIONS;
        }
        return List.of();
    }
}
