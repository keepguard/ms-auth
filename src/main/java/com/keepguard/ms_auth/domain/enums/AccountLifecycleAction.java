package com.keepguard.ms_auth.domain.enums;

import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;

public enum AccountLifecycleAction {
    BLOCK,
    UNBLOCK,
    DELETE;

    public String userAuthority() {
        return switch (this) {
            case BLOCK -> SystemAuthorityNames.USER_BLOCK;
            case UNBLOCK -> SystemAuthorityNames.USER_UNBLOCK;
            case DELETE -> SystemAuthorityNames.USER_DELETE;
        };
    }

    public String managerAuthority() {
        return switch (this) {
            case BLOCK -> SystemAuthorityNames.MANAGER_BLOCK;
            case UNBLOCK -> SystemAuthorityNames.MANAGER_UNBLOCK;
            case DELETE -> SystemAuthorityNames.MANAGER_DELETE;
        };
    }
}
