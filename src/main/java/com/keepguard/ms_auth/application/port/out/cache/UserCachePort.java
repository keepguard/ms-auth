package com.keepguard.ms_auth.application.port.out.cache;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.User;

public interface UserCachePort {

    // By Username
    void cacheUserByUsername(String username, UserGetByUsernameView user);
    UserAuthCacheView getUserByUsernameFromCache(String username);
    void removeUserFromCacheByUsername(String username);

    // By Email
    void cacheUserByEmail(String email, UserGetByEmailView user);
    UserAuthCacheView getUserByEmailFromCache(String email);
    void removeUserFromCacheByEmail(String email);

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
