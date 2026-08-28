package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.port.in.UserPort;
import com.keepguard.ms_auth.application.service.exception.RateLimitExceededException;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUseCaseService implements UserPort {

    private final UserCommandService commandService;
    private final UserQueryService queryService;

    @Override
    public UserView create(UserCreateCommandDTO command) {
        return commandService.create(command);
    }

    @Override
    public UserView createAdmin(UserCreateCommandDTO command) {
        return commandService.createAdmin(command);
    }

    @Override
    @RateLimiter(name = "createManager", fallbackMethod = "createManagerRateLimitExceeded")
    public UserView createManager(UserCreateCommandDTO command) {
        return commandService.createManager(command);
    }

    @Override
    @RateLimiter(name = "accountLifecycle", fallbackMethod = "accountLifecycleRateLimitExceeded")
    public void delete(UserDeleteCommandDTO command) {
        commandService.delete(command);
    }

    @Override
    @RateLimiter(name = "accountLifecycle", fallbackMethod = "accountLifecycleRateLimitExceeded")
    public void block(UserBlockCommandDTO command) {
        commandService.block(command);
    }

    @Override
    @RateLimiter(name = "accountLifecycle", fallbackMethod = "accountLifecycleRateLimitExceeded")
    public void unlock(UserUnlockCommandDTO command) {
        commandService.unlock(command);
    }

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

    private UserView createManagerRateLimitExceeded(UserCreateCommandDTO command, RequestNotPermitted ex) {
        log.warn("RATE LIMIT EXCEDIDO | createManager | username={}", command != null ? command.getUsername() : null);
        throw new RateLimitExceededException("Muitas tentativas de criar manager. Aguarde antes de tentar novamente.");
    }

    private void accountLifecycleRateLimitExceeded(UserDeleteCommandDTO command, RequestNotPermitted ex) {
        log.warn("RATE LIMIT EXCEDIDO | delete | idUserExternal={}", command != null ? command.getIdUserExternal() : null);
        throw new RateLimitExceededException("Muitas tentativas de alterar status da conta. Aguarde antes de tentar novamente.");
    }

    private void accountLifecycleRateLimitExceeded(UserBlockCommandDTO command, RequestNotPermitted ex) {
        log.warn("RATE LIMIT EXCEDIDO | block | idUserExternal={}", command != null ? command.getIdUserExternal() : null);
        throw new RateLimitExceededException("Muitas tentativas de alterar status da conta. Aguarde antes de tentar novamente.");
    }

    private void accountLifecycleRateLimitExceeded(UserUnlockCommandDTO command, RequestNotPermitted ex) {
        log.warn("RATE LIMIT EXCEDIDO | unlock | idUserExternal={}", command != null ? command.getIdUserExternal() : null);
        throw new RateLimitExceededException("Muitas tentativas de alterar status da conta. Aguarde antes de tentar novamente.");
    }
}
