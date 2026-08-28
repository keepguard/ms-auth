package com.keepguard.ms_auth.domain.enums;

import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;

public enum AccountLifecycleRank {
    NONE(0),
    USER(10),
    MANAGER(50),
    ADMIN(90),
    SYSTEM(100);

    private final int order;

    AccountLifecycleRank(int order) {
        this.order = order;
    }

    public int order() {
        return order;
    }

    public static AccountLifecycleRank fromRoleName(String roleName) {
        if (roleName == null) {
            return NONE;
        }
        return switch (roleName.trim().toUpperCase()) {
            case SystemRoleNames.ROLE_SYSTEM -> SYSTEM;
            case SystemRoleNames.ROLE_ADMIN -> ADMIN;
            case SystemRoleNames.ROLE_MANAGER -> MANAGER;
            case SystemRoleNames.ROLE_USER -> USER;
            default -> NONE;
        };
    }

    public static AccountLifecycleRank max(AccountLifecycleRank left, AccountLifecycleRank right) {
        if (left == null) {
            return right == null ? NONE : right;
        }
        if (right == null) {
            return left;
        }
        return left.order >= right.order ? left : right;
    }
}
