package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.ForbiddenException;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.user.UserRole;
import com.keepguard.ms_auth.domain.enums.AccountLifecycleAction;
import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Lifecycle Policy Tests")
class AccountLifecyclePolicyTest {

    @Mock private UserRoleRepositoryPort userRoleRepository;
    @Mock private RoleRepositoryPort roleRepository;
    @Mock private UserRepositoryPort userRepository;

    @InjectMocks
    private AccountLifecyclePolicy policy;

    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    @DisplayName("USER sem authority não apaga a si")
    void userWithoutAuthorityCannotDeleteSelf() {
        User user = user(SystemRoleNames.ROLE_USER);
        stubRoles(user, SystemRoleNames.ROLE_USER);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
            () -> policy.assertAllowed(user, user, AccountLifecycleAction.DELETE));
        assertEquals("MISSING_AUTHORITY", ex.getErrorCode());
    }

    @Test
    @DisplayName("USER com user:delete apaga só a si")
    void userWithDeleteCanDeleteSelf() {
        User user = user(SystemRoleNames.ROLE_USER);
        stubRoles(user, SystemRoleNames.ROLE_USER, SystemAuthorityNames.USER_DELETE);

        assertDoesNotThrow(() -> policy.assertAllowed(user, user, AccountLifecycleAction.DELETE));
    }

    @Test
    @DisplayName("USER com user:delete não apaga outro USER")
    void userCannotDeleteAnotherUser() {
        User actor = user(SystemRoleNames.ROLE_USER);
        User target = user(SystemRoleNames.ROLE_USER);
        stubRoles(actor, SystemRoleNames.ROLE_USER, SystemAuthorityNames.USER_DELETE);
        stubRoles(target, SystemRoleNames.ROLE_USER);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
            () -> policy.assertAllowed(actor, target, AccountLifecycleAction.DELETE));
        assertEquals("SELF_ONLY", ex.getErrorCode());
    }

    @Test
    @DisplayName("MANAGER com user:* age em USER e 403 em MANAGER")
    void managerWithUserAuthoritiesActsOnUserNotManager() {
        User manager = user(SystemRoleNames.ROLE_MANAGER);
        User targetUser = user(SystemRoleNames.ROLE_USER);
        User targetManager = user(SystemRoleNames.ROLE_MANAGER);
        stubRoles(manager, SystemRoleNames.ROLE_MANAGER,
            SystemAuthorityNames.USER_BLOCK, SystemAuthorityNames.USER_UNBLOCK, SystemAuthorityNames.USER_DELETE);
        stubRoles(targetUser, SystemRoleNames.ROLE_USER);
        stubRoles(targetManager, SystemRoleNames.ROLE_MANAGER);

        assertDoesNotThrow(() -> policy.assertAllowed(manager, targetUser, AccountLifecycleAction.BLOCK));
        ForbiddenException ex = assertThrows(ForbiddenException.class,
            () -> policy.assertAllowed(manager, targetManager, AccountLifecycleAction.BLOCK));
        assertEquals("MISSING_AUTHORITY", ex.getErrorCode());
    }

    @Test
    @DisplayName("MANAGER com manager:block age em MANAGER")
    void managerWithManagerBlockActsOnManager() {
        User manager = user(SystemRoleNames.ROLE_MANAGER);
        User target = user(SystemRoleNames.ROLE_MANAGER);
        stubRoles(manager, SystemRoleNames.ROLE_MANAGER,
            SystemAuthorityNames.USER_BLOCK, SystemAuthorityNames.MANAGER_BLOCK);
        stubRoles(target, SystemRoleNames.ROLE_MANAGER);

        assertDoesNotThrow(() -> policy.assertAllowed(manager, target, AccountLifecycleAction.BLOCK));
    }

    @Test
    @DisplayName("ADMIN age em USER e MANAGER e 403 em ADMIN")
    void adminActsOnUserAndManagerButNotAdmin() {
        User admin = user(SystemRoleNames.ROLE_ADMIN);
        User targetUser = user(SystemRoleNames.ROLE_USER);
        User targetManager = user(SystemRoleNames.ROLE_MANAGER);
        User targetAdmin = user(SystemRoleNames.ROLE_ADMIN);
        stubRoles(admin, SystemRoleNames.ROLE_ADMIN,
            SystemAuthorityNames.USER_BLOCK, SystemAuthorityNames.MANAGER_BLOCK);
        stubRoles(targetUser, SystemRoleNames.ROLE_USER);
        stubRoles(targetManager, SystemRoleNames.ROLE_MANAGER);
        stubRoles(targetAdmin, SystemRoleNames.ROLE_ADMIN);

        assertDoesNotThrow(() -> policy.assertAllowed(admin, targetUser, AccountLifecycleAction.BLOCK));
        assertDoesNotThrow(() -> policy.assertAllowed(admin, targetManager, AccountLifecycleAction.BLOCK));
        ForbiddenException ex = assertThrows(ForbiddenException.class,
            () -> policy.assertAllowed(admin, targetAdmin, AccountLifecycleAction.BLOCK));
        assertEquals("TARGET_PROTECTED", ex.getErrorCode());
    }

    @Test
    @DisplayName("SYSTEM não é alvo")
    void systemIsProtected() {
        User admin = user(SystemRoleNames.ROLE_ADMIN);
        User system = user(SystemRoleNames.ROLE_SYSTEM);
        stubRoles(admin, SystemRoleNames.ROLE_ADMIN, SystemAuthorityNames.USER_BLOCK);
        stubRoles(system, SystemRoleNames.ROLE_SYSTEM);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
            () -> policy.assertAllowed(admin, system, AccountLifecycleAction.BLOCK));
        assertEquals("TARGET_PROTECTED", ex.getErrorCode());
    }

    @Test
    @DisplayName("USER+MANAGER no alvo conta como MANAGER")
    void highestRankWins() {
        User manager = user(SystemRoleNames.ROLE_MANAGER);
        User target = user(SystemRoleNames.ROLE_USER);
        stubRoles(manager, SystemRoleNames.ROLE_MANAGER, SystemAuthorityNames.USER_BLOCK);
        stubRoles(target, List.of(SystemRoleNames.ROLE_USER, SystemRoleNames.ROLE_MANAGER), Set.of());

        ForbiddenException ex = assertThrows(ForbiddenException.class,
            () -> policy.assertAllowed(manager, target, AccountLifecycleAction.BLOCK));
        assertEquals("MISSING_AUTHORITY", ex.getErrorCode());
    }

    @Test
    @DisplayName("MANAGER não age em si mesmo")
    void managerCannotActOnSelf() {
        User manager = user(SystemRoleNames.ROLE_MANAGER);
        stubRoles(manager, SystemRoleNames.ROLE_MANAGER,
            SystemAuthorityNames.USER_DELETE, SystemAuthorityNames.MANAGER_DELETE);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
            () -> policy.assertAllowed(manager, manager, AccountLifecycleAction.DELETE));
        assertEquals("SELF_ACTION_FORBIDDEN", ex.getErrorCode());
    }

    private User user(String ignoredRoleName) {
        return UserTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCompanyId(companyId)
            .withTenantId(companyId)
            .buildDomain();
    }

    private void stubRoles(User user, String roleName, String... authorityNames) {
        stubRoles(user, List.of(roleName), Set.of(authorityNames));
    }

    private void stubRoles(User user, List<String> roleNames, Set<String> authorityNames) {
        List<UserRole> assignments = roleNames.stream()
            .map(name -> {
                Role role = RoleTestBuilder.builder()
                    .withCompanyId(companyId)
                    .withName(name)
                    .buildDomain();
                Set<Authority> authorities = new HashSet<>();
                for (String authorityName : authorityNames) {
                    if (roleNames.size() == 1 || SystemRoleNames.ROLE_MANAGER.equals(name) || SystemRoleNames.ROLE_ADMIN.equals(name)) {
                        authorities.add(Authority.builder().id(UUID.randomUUID()).name(authorityName).companyId(companyId).build());
                    }
                }
                if (SystemRoleNames.ROLE_USER.equals(name) && roleNames.size() > 1) {
                    role.setAuthorities(new HashSet<>());
                } else {
                    role.setAuthorities(authorities);
                }
                when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
                return UserRole.assign(user.getId(), role.getId());
            })
            .toList();
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(assignments);
    }
}
