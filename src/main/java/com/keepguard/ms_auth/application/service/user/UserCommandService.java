package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.dto.user.UserView;
import com.keepguard.ms_auth.application.mapper.UserApplicationMapper;
import com.keepguard.ms_auth.application.port.out.cache.UserCachePort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserStatusHistoryRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.ms_auth.application.dto.user.UserHardDeleteCommandDTO;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatusEventType;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import com.keepguard.ms_auth.domain.entity.user.UserRole;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.lib_common.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCommandService {

    private static final String DEFAULT_USER_ROLE = "USER";

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final UserRoleRepositoryPort userRoleRepository;
    private final UserStatusHistoryRepositoryPort userStatusHistoryRepository;
    private final UserApplicationMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MetricsPort metricsPort;
    private final UserCachePort userCachePort;

    @LogOperation(
        operation = "CREATE_USER",
        description = "Criando novo usuário: {command.username}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "USER"
    )
    @Transactional
    public UserView create(UserCreateCommandDTO command) {
        log.info("Creating user with username: {}", command.getUsername());

        if (userRepository.findByUsernameAndXApplication(command.getUsername(), command.getXApplicationUuid()).isPresent()) {
            metricsPort.incrementCounter("user_business_errors_total",
                Map.of("error_code", "USERNAME_ALREADY_EXISTS", "operation", "create"));
            throw new AlreadyExistsException("Username já existe: " + command.getUsername());
        }

        if (userRepository.findByEmailAndXApplication(command.getEmail(), command.getXApplicationUuid()).isPresent()) {
            metricsPort.incrementCounter("user_business_errors_total",
                Map.of("error_code", "EMAIL_ALREADY_EXISTS", "operation", "create"));
            throw new AlreadyExistsException("Email já existe: " + command.getEmail());
        }

        var idUserExternalUuid = UUID.fromString(command.getIdUserExternal());
        if (userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.getXApplicationUuid()).isPresent()) {
            metricsPort.incrementCounter("user_business_errors_total",
                Map.of("error_code", "ID_EXTERNAL_ALREADY_EXISTS", "operation", "create"));
            throw new AlreadyExistsException("ID externo já existe: " + command.getIdUserExternal());
        }

        User user = User.createNew(
            command.getUsername(),
            command.getEmail(),
            command.getPassword(),
            idUserExternalUuid,
            command.getCodeUser(),
            command.getCompanyId(),
            command.getCompanyCode(),
            command.getXApplicationUuid()
        );

        User savedUser = userRepository.save(user);

        if (command.getRoles() != null) {
            for (String roleName : command.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role não encontrada: " + roleName));
                userRoleRepository.save(UserRole.assign(savedUser.getId(), role.getId()));
            }
        }

        userStatusHistoryRepository.save(
            UserStatusHistory.create(savedUser.getId(), UserStatusEventType.CREATED, "Usuário criado")
        );

        // Buscar e adicionar role USER se existir
        roleRepository.findByName(DEFAULT_USER_ROLE).ifPresent(role -> {
            userRoleRepository.save(UserRole.assign(savedUser.getId(), role.getId()));
            log.info("Role {} added to user with ID: {}", DEFAULT_USER_ROLE, savedUser.getId());
        });

        metricsPort.incrementCounter("user_created_total",
            Map.of("status", "success"));

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toView(savedUser);
    }

    @LogOperation(
        operation = "DELETE_USER",
        description = "Removendo usuário: {command.idUserExternal}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "USER"
    )
    @Transactional
    public void delete(UserDeleteCommandDTO command) {
        log.info("Deleting user with external ID: {}", command.getIdUserExternal());

        UUID idUserExternalUuid = ValidationUtils.validateAndParseUUID(command.getIdUserExternal());
        User user = userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.getXApplicationUuid())
            .orElseThrow(() -> {
                metricsPort.incrementCounter("user_business_errors_total",
                    Map.of("error_code", "USER_NOT_FOUND", "operation", "delete"));
                return new NotFoundException("Usuário não encontrado: " + command.getIdUserExternal());
            });

        user.markAsDeleted();
        userRepository.save(user);

        userStatusHistoryRepository.save(
            UserStatusHistory.create(user.getId(), UserStatusEventType.DELETED, command.getReason())
        );

        userCachePort.removeUserFromCache(user);

        metricsPort.incrementCounter("user_deleted_total",
            Map.of("status", "success"));

        log.info("User deleted successfully with ID: {}", user.getId());
    }

    @LogOperation(
        operation = "HARD_DELETE_USER",
        description = "Hard delete usuário: {command.idUserExternal}",
        audit = true,
        auditAction = "HARD_DELETE",
        auditEntityType = "USER"
    )
    @Transactional
    public void hardDelete(UserHardDeleteCommandDTO command) {
        log.info("Hard deleting user with external ID: {}", command.idUserExternal());

        UUID idUserExternalUuid = ValidationUtils.validateAndParseUUID(command.idUserExternal());
        User user = userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.xApplicationUuid())
            .orElseThrow(() -> {
                metricsPort.incrementCounter("user_business_errors_total",
                    Map.of("error_code", "USER_NOT_FOUND", "operation", "hard_delete"));
                return new NotFoundException("Usuário não encontrado: " + command.idUserExternal());
            });

        // Remover roles do usuário
        userRoleRepository.deleteByUserId(user.getId());

        // Remover histórico de status
        userStatusHistoryRepository.deleteByUserId(user.getId());

        // Deletar usuário fisicamente
        userRepository.deleteById(user.getId());

        // Remover do cache
        userCachePort.removeUserFromCache(user);

        metricsPort.incrementCounter("user_hard_deleted_total",
            Map.of("status", "success"));

        log.info("User hard deleted successfully with ID: {}", user.getId());
    }

    @LogOperation(
        operation = "BLOCK_USER",
        description = "Bloqueando usuário: {command.idUserExternal}",
        audit = true,
        auditAction = "BLOCK",
        auditEntityType = "USER"
    )
    @Transactional
    public void block(UserBlockCommandDTO command) {
        log.info("Blocking user with external ID: {}", command.getIdUserExternal());

        UUID idUserExternalUuid = ValidationUtils.validateAndParseUUID(command.getIdUserExternal());
        User user = userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.getXApplicationUuid())
            .orElseThrow(() -> {
                metricsPort.incrementCounter("user_business_errors_total",
                    Map.of("error_code", "USER_NOT_FOUND", "operation", "block"));
                return new NotFoundException("Usuário não encontrado: " + command.getIdUserExternal());
            });

        user.block();
        userRepository.save(user);

        userStatusHistoryRepository.save(
            UserStatusHistory.create(user.getId(), UserStatusEventType.BLOCKED, command.getReason())
        );

        userCachePort.removeUserFromCache(user);

        metricsPort.incrementCounter("user_blocked_total",
            Map.of("status", "success"));

        log.info("User blocked successfully with ID: {}", user.getId());
    }

    @LogOperation(
        operation = "UNLOCK_USER",
        description = "Desbloqueando usuário: {command.idUserExternal}",
        audit = true,
        auditAction = "UNLOCK",
        auditEntityType = "USER"
    )
    @Transactional
    public void unlock(UserUnlockCommandDTO command) {
        log.info("Unlocking user with external ID: {}", command.getIdUserExternal());

        UUID idUserExternalUuid = ValidationUtils.validateAndParseUUID(command.getIdUserExternal());
        User user = userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.getXApplicationUuid())
            .orElseThrow(() -> {
                metricsPort.incrementCounter("user_business_errors_total",
                    Map.of("error_code", "USER_NOT_FOUND", "operation", "unlock"));
                return new NotFoundException("Usuário não encontrado: " + command.getIdUserExternal());
            });

        user.unlock();
        userRepository.save(user);

        userStatusHistoryRepository.save(
            UserStatusHistory.create(user.getId(), UserStatusEventType.UNLOCKED, command.getReason())
        );

        userCachePort.removeUserFromCache(user);

        metricsPort.incrementCounter("user_unlocked_total",
            Map.of("status", "success"));

        log.info("User unlocked successfully with ID: {}", user.getId());
    }

    @LogOperation(
        operation = "VALIDATE_EMAIL_USER",
        description = "Validando email do usuário: {command.idUserExternal}",
        audit = true,
        auditAction = "VALIDATE_EMAIL",
        auditEntityType = "USER"
    )
    @Transactional
    public void validateEmailUser(UserValidateEmailCommandDTO command) {
        log.info("Validating email for user with external ID: {}", command.getIdUserExternal());

        UUID idUserExternalUuid = ValidationUtils.validateAndParseUUID(command.getIdUserExternal());
        User user = userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.getXApplicationUuid())
            .orElseThrow(() -> {
                metricsPort.incrementCounter("user_business_errors_total",
                    Map.of("error_code", "USER_NOT_FOUND", "operation", "validate_email"));
                return new NotFoundException("Usuário não encontrado: " + command.getIdUserExternal());
            });

        user.validateEmail();
        userRepository.save(user);

        userStatusHistoryRepository.save(
            UserStatusHistory.create(user.getId(), UserStatusEventType.EMAIL_VALIDATED, "Email validado com sucesso")
        );

        userCachePort.removeUserFromCache(user);

        metricsPort.incrementCounter("user_email_validated_total",
            Map.of("status", "success"));

        log.info("User email validated successfully with ID: {}", user.getId());
    }

    @Transactional
    public void addRoleToUser(UserAddRoleCommandDTO command) {
        log.info("Adding role {} to user with external ID: {}", command.getRole(), command.getIdUserExternal());

        UUID idUserExternalUuid = ValidationUtils.validateAndParseUUID(command.getIdUserExternal());
        User user = userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.getXApplicationUuid())
            .orElseThrow(() -> {
                metricsPort.incrementCounter("user_business_errors_total",
                    Map.of("error_code", "USER_NOT_FOUND", "operation", "add_role"));
                return new NotFoundException("Usuário não encontrado: " + command.getIdUserExternal());
            });

        Role role = roleRepository.findByName(command.getRole())
            .orElseThrow(() -> new NotFoundException("Role não encontrada: " + command.getRole()));

        if (userRoleRepository.findByUserIdAndRoleId(user.getId(), role.getId()).isPresent()) {
            metricsPort.incrementCounter("user_business_errors_total",
                Map.of("error_code", "ROLE_ALREADY_ASSIGNED", "operation", "add_role"));
            throw new AlreadyExistsException("Usuário já possui a role: " + command.getRole());
        }

        userRoleRepository.save(UserRole.assign(user.getId(), role.getId()));

        userCachePort.removeUserFromCache(user);

        metricsPort.incrementCounter("user_role_added_total",
            Map.of("status", "success"));

        log.info("Role {} added successfully to user with ID: {}", command.getRole(), user.getId());
    }

    @Transactional
    public void removeRoleFromUser(UserRemoveRoleCommandDTO command) {
        log.info("Removing role {} from user with external ID: {}", command.getRole(), command.getIdUserExternal());

        UUID idUserExternalUuid = ValidationUtils.validateAndParseUUID(command.getIdUserExternal());
        User user = userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.getXApplicationUuid())
            .orElseThrow(() -> {
                metricsPort.incrementCounter("user_business_errors_total",
                    Map.of("error_code", "USER_NOT_FOUND", "operation", "remove_role"));
                return new NotFoundException("Usuário não encontrado: " + command.getIdUserExternal());
            });

        Role role = roleRepository.findByName(command.getRole())
            .orElseThrow(() -> new NotFoundException("Role não encontrada: " + command.getRole()));

        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(user.getId(), role.getId())
            .orElseThrow(() -> new NotFoundException("Associação usuário-role não encontrada"));

        userRoleRepository.delete(userRole);

        userCachePort.removeUserFromCache(user);

        metricsPort.incrementCounter("user_role_removed_total",
            Map.of("status", "success"));

        log.info("Role {} removed successfully from user with ID: {}", command.getRole(), user.getId());
    }


    @Transactional
    public void updateUserEmail(UserUpdateEmailCommandDTO command) {
        log.info("Updating email for user with external ID: {} to: {}", command.getIdUserExternal(), command.getNewEmail());

        UUID idUserExternalUuid = ValidationUtils.validateAndParseUUID(command.getIdUserExternal());
        User user = userRepository.findByIdUserExternalAndXApplication(idUserExternalUuid, command.getXApplicationUuid())
            .orElseThrow(() -> {
                metricsPort.incrementCounter("user_business_errors_total",
                    Map.of("error_code", "USER_NOT_FOUND", "operation", "update_email"));
                return new NotFoundException("Usuário não encontrado: " + command.getIdUserExternal());
            });


        if (userRepository.findByEmailAndXApplication(command.getNewEmail(), command.getXApplicationUuid()).isPresent()) {
            metricsPort.incrementCounter("user_business_errors_total",
                Map.of("error_code", "EMAIL_ALREADY_EXISTS", "operation", "update_email"));
            throw new AlreadyExistsException("Email já existe: " + command.getNewEmail());
        }

        String oldEmail = user.getEmail();
        user.updateEmail(command.getNewEmail());
        userRepository.save(user);

        userStatusHistoryRepository.save(
            UserStatusHistory.create(user.getId(), UserStatusEventType.EMAIL_UPDATED,
                "Email atualizado de " + oldEmail + " para " + command.getNewEmail())
        );

        userCachePort.removeUserFromCache(user);

        metricsPort.incrementCounter("user_email_updated_total",
            Map.of("status", "success"));

        log.info("User email updated successfully with ID: {}", user.getId());
    }

}
