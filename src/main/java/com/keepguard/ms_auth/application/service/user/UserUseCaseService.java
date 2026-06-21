package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.port.in.UserPort;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUseCaseService implements UserPort {

    private final UserCommandService commandService;
    private final UserQueryService queryService;

    // === OPERAÇÕES DE COMANDO ===

    @Override
    public UserView create(UserCreateCommandDTO command) {
        return commandService.create(command);
    }

    @Override
    public void delete(UserDeleteCommandDTO command) {
        commandService.delete(command);
    }

    @Override
    public void block(UserBlockCommandDTO command) {
        commandService.block(command);
    }

    @Override
    public void unlock(UserUnlockCommandDTO command) {
        commandService.unlock(command);
    }

    // === OPERAÇÕES DE CONSULTA ===

    @Override
    public UserGetByUsernameView findByUsername(UserGetByUsernameQueryDTO query) {
        return queryService.findByUsername(query);
    }

    @Override
    public UserGetByEmailView findByEmail(UserGetByEmailQueryDTO query) {
        return queryService.findByEmail(query);
    }

    @Override
    public UserGetByCodeView findByCodeUser(UserGetByCodeQueryDTO query) {
        return queryService.findByCodeUser(query);
    }

    @Override
    public UserGetByIdExternalView findByIdUserExternal(UserGetByIdExternalQueryDTO query) {
        return queryService.findByIdUserExternal(query);
    }

    @Override
    public PageResultView<UserStatusHistory> getUserStatusHistory(UserGetStatusHistoryQueryDTO query) {
        return queryService.getUserStatusHistory(query);
    }

    @Override
    public PageResultView<UserSearchView> searchUsers(UserSearchQueryDTO query) {
        return queryService.searchUsers(query);
    }

    // === OPERAÇÕES DE COMANDO ADICIONAIS ===
    // Operações de gerenciamento de usuário implementadas seguindo padrão CQRS

    @Override
    public void validateEmailUser(UserValidateEmailCommandDTO command) {
        commandService.validateEmailUser(command);
    }

    @Override
    public void addRoleToUser(UserAddRoleCommandDTO command) {
        commandService.addRoleToUser(command);
    }

    @Override
    public void removeRoleFromUser(UserRemoveRoleCommandDTO command) {
        commandService.removeRoleFromUser(command);
    }

    @Override
    public void updateUserEmail(UserUpdateEmailCommandDTO command) {
        commandService.updateUserEmail(command);
    }

    @Override
    public void hardDelete(UserHardDeleteCommandDTO command) {
        commandService.hardDelete(command);
    }
}
