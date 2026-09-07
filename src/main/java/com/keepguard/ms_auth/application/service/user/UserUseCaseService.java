package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.dto.common.PageResultViewDTO;
import com.keepguard.ms_auth.application.port.in.UserPort;
import com.keepguard.ms_auth.application.service.exception.RateLimitExceededException;
import com.keepguard.ms_auth.application.dto.user.*;
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
    public UserViewDTO create(UserCreateCommandDTO command) {
        return commandService.create(command);
    }

    @Override
    public UserViewDTO createAdmin(UserCreateCommandDTO command) {
        return commandService.createAdmin(command);
    }

    @Override
    @RateLimiter(name = "createManager", fallbackMethod = "createManagerRateLimitExceeded")
    public UserViewDTO createManager(UserCreateCommandDTO command) {
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
    public UserGetByUsernameViewDTO findByUsername(UserGetByUsernameQueryDTO query) {
        return queryService.findByUsername(query);
    }

    @Override
    public UserGetByEmailViewDTO findByEmail(UserGetByEmailQueryDTO query) {
        return queryService.findByEmail(query);
    }

    @Override
    public UserGetByCodeViewDTO findByCodeUser(UserGetByCodeQueryDTO query) {
        return queryService.findByCodeUser(query);
    }

    @Override
    public UserGetByIdExternalViewDTO findByIdUserExternal(UserGetByIdExternalQueryDTO query) {
        return queryService.findByIdUserExternal(query);
    }

    @Override
    public PageResultViewDTO<UserStatusHistory> getUserStatusHistory(UserGetStatusHistoryQueryDTO query) {
        return queryService.getUserStatusHistory(query);
    }

    @Override
    public PageResultViewDTO<UserSearchViewDTO> searchUsers(UserSearchQueryDTO query) {
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

    private UserViewDTO createManagerRateLimitExceeded(UserCreateCommandDTO command, RequestNotPermitted ex) {
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
