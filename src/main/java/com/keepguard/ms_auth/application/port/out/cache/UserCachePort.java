package com.keepguard.ms_auth.application.port.out.cache;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.User;

import java.util.UUID;

public interface UserCachePort {

    // By Username
    void cacheUserByUsername(UUID companyId, String username, UserGetByUsernameView user);
    UserAuthCacheView getUserByUsernameFromCache(UUID companyId, String username);
    void removeUserFromCacheByUsername(UUID companyId, String username);

    // By Email
    void cacheUserByEmail(UUID companyId, String email, UserGetByEmailView user);
    UserAuthCacheView getUserByEmailFromCache(UUID companyId, String email);
    void removeUserFromCacheByEmail(UUID companyId, String email);

    // By CodeUser
    void cacheUserByCodeUser(String codeUser, UserGetByCodeView user);
    UserAuthCacheView getUserByCodeUserFromCache(String codeUser);
    void removeUserFromCacheByCodeUser(String codeUser);

    // By IdExternal
    void cacheUserByIdExternal(String idUserExternal, UserAuthCacheView user);
    UserAuthCacheView getUserByIdExternalFromCache(String idUserExternal);
    void removeUserFromCacheByIdExternal(String idUserExternal);

    // User Roles
    void cacheUserRoles(String codeUser, UserRolesCacheView userRoles);
    UserRolesCacheView getUserRolesFromCache(String codeUser);
    void removeUserRolesFromCache(String codeUser);

    // Clear All
    void clearAllUserCache();
    void removeUserFromCache(User user);


}
