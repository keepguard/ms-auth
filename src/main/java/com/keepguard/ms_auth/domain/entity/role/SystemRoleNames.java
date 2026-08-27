package com.keepguard.ms_auth.domain.entity.role;

import java.util.List;
import java.util.Set;

public final class SystemRoleNames {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_SYSTEM = "ROLE_SYSTEM";

    public static final List<String> TEMPLATES = List.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_USER, ROLE_SYSTEM);
    public static final List<String> PROVISIONED = List.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_USER);
    public static final Set<String> RESERVED = Set.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_USER, ROLE_SYSTEM);

    private SystemRoleNames() {
    }

    public static boolean isReserved(String name) {
        return name != null && RESERVED.contains(name.trim().toUpperCase());
    }
}
