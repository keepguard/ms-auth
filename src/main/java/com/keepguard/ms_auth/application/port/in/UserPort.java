package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;

public interface UserPort {

    UserView create(UserCreateCommandDTO command);

    void delete(UserDeleteCommandDTO command);

    void hardDelete(UserHardDeleteCommandDTO command);

    void block(UserBlockCommandDTO command);

    void unlock(UserUnlockCommandDTO command);

    void validateEmailUser(UserValidateEmailCommandDTO command);

    PageResultView<UserStatusHistory> getUserStatusHistory(UserGetStatusHistoryQueryDTO query);

    UserGetByUsernameView findByUsername(UserGetByUsernameQueryDTO query);

    UserGetByIdExternalView findByIdUserExternal(UserGetByIdExternalQueryDTO query);

    UserGetByEmailView findByEmail(UserGetByEmailQueryDTO query);

    UserGetByCodeView findByCodeUser(UserGetByCodeQueryDTO query);

    void addRoleToUser(UserAddRoleCommandDTO command);

    void removeRoleFromUser(UserRemoveRoleCommandDTO command);

    PageResultView<UserSearchView> searchUsers(UserSearchQueryDTO query);

    void updateUserEmail(UserUpdateEmailCommandDTO command);
}
