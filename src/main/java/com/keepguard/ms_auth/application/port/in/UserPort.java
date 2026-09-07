package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.dto.common.PageResultViewDTO;
import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;

public interface UserPort {

    UserViewDTO create(UserCreateCommandDTO command);

    UserViewDTO createAdmin(UserCreateCommandDTO command);

    UserViewDTO createManager(UserCreateCommandDTO command);

    void delete(UserDeleteCommandDTO command);

    void hardDelete(UserHardDeleteCommandDTO command);

    void block(UserBlockCommandDTO command);

    void unlock(UserUnlockCommandDTO command);

    void validateEmailUser(UserValidateEmailCommandDTO command);

    PageResultViewDTO<UserStatusHistory> getUserStatusHistory(UserGetStatusHistoryQueryDTO query);

    UserGetByUsernameViewDTO findByUsername(UserGetByUsernameQueryDTO query);

    UserGetByIdExternalViewDTO findByIdUserExternal(UserGetByIdExternalQueryDTO query);

    UserGetByEmailViewDTO findByEmail(UserGetByEmailQueryDTO query);

    UserGetByCodeViewDTO findByCodeUser(UserGetByCodeQueryDTO query);

    void addRoleToUser(UserAddRoleCommandDTO command);

    void removeRoleFromUser(UserRemoveRoleCommandDTO command);

    PageResultViewDTO<UserSearchViewDTO> searchUsers(UserSearchQueryDTO query);

    void updateUserEmail(UserUpdateEmailCommandDTO command);
}
