package com.keepguard.ms_auth.application.port.out.cache;

import com.keepguard.ms_auth.application.dto.role.RoleCacheView;

public interface RoleCachePort {

    // By RoleId
    void cacheRoleById(String roleId, RoleCacheView role);
    RoleCacheView getRoleByIdFromCache(String roleId);
    void removeRoleFromCacheById(String roleId);

    // Clear All
    void clearAllRoleCache();

}
