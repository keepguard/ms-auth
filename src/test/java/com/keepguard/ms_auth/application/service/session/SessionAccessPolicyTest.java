package com.keepguard.ms_auth.application.service.session;

import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.ForbiddenException;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.user.UserRole;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Session Access Policy Tests")
class SessionAccessPolicyTest {

    @Mock private UserRoleRepositoryPort userRoleRepository;
    @Mock private RoleRepositoryPort roleRepository;

    @InjectMocks
    private SessionAccessPolicy policy;

    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
    }

    @Test
    @DisplayName("USER lê e escreve só a si")
    void userCanReadAndWriteSelf() {
        User user = user();
        stubRoles(user, SystemRoleNames.ROLE_USER);

        assertDoesNotThrow(() -> policy.assertCanRead(user, user));
        assertDoesNotThrow(() -> policy.assertCanWrite(user, user));
        assertTrue(policy.canWrite(user, user));
    }

    @Test
    @DisplayName("USER não lê nem escreve outro USER")
    void userCannotAccessAnotherUser() {
        User actor = user();
        User target = user();
        stubRoles(actor, SystemRoleNames.ROLE_USER);
        stubRoles(target, SystemRoleNames.ROLE_USER);

        ForbiddenException read = assertThrows(ForbiddenException.class,
                () -> policy.assertCanRead(actor, target));
        assertEquals("SELF_ONLY", read.getErrorCode());

        ForbiddenException write = assertThrows(ForbiddenException.class,
                () -> policy.assertCanWrite(actor, target));
        assertEquals("SELF_ONLY", write.getErrorCode());
        assertFalse(policy.canWrite(actor, target));
    }

    @Test
    @DisplayName("USER não lista o tenant")
    void userCannotListTenant() {
        User user = user();
        stubRoles(user, SystemRoleNames.ROLE_USER);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> policy.assertCanListTenant(user));
        assertEquals("SESSION_FORBIDDEN", ex.getErrorCode());
    }

    @Test
    @DisplayName("MANAGER lê USER, MANAGER, ADMIN e SYSTEM do tenant")
    void managerCanReadAnyoneInTenant() {
        User manager = user();
        User targetUser = user();
        User targetManager = user();
        User targetAdmin = user();
        User targetSystem = user();
        stubRoles(manager, SystemRoleNames.ROLE_MANAGER);
        stubRoles(targetUser, SystemRoleNames.ROLE_USER);
        stubRoles(targetManager, SystemRoleNames.ROLE_MANAGER);
        stubRoles(targetAdmin, SystemRoleNames.ROLE_ADMIN);
        stubRoles(targetSystem, SystemRoleNames.ROLE_SYSTEM);

        assertDoesNotThrow(() -> policy.assertCanRead(manager, targetUser));
        assertDoesNotThrow(() -> policy.assertCanRead(manager, targetManager));
        assertDoesNotThrow(() -> policy.assertCanRead(manager, targetAdmin));
        assertDoesNotThrow(() -> policy.assertCanRead(manager, targetSystem));
        assertDoesNotThrow(() -> policy.assertCanListTenant(manager));
    }

    @Test
    @DisplayName("MANAGER escreve em USER e em si, mas não em MANAGER/ADMIN/SYSTEM")
    void managerWritesOnlyUserAndSelf() {
        User manager = user();
        User otherManager = user();
        User targetUser = user();
        User targetAdmin = user();
        User targetSystem = user();
        stubRoles(manager, SystemRoleNames.ROLE_MANAGER);
        stubRoles(otherManager, SystemRoleNames.ROLE_MANAGER);
        stubRoles(targetUser, SystemRoleNames.ROLE_USER);
        stubRoles(targetAdmin, SystemRoleNames.ROLE_ADMIN);
        stubRoles(targetSystem, SystemRoleNames.ROLE_SYSTEM);

        assertDoesNotThrow(() -> policy.assertCanWrite(manager, manager));
        assertDoesNotThrow(() -> policy.assertCanWrite(manager, targetUser));
        assertTrue(policy.canWrite(manager, targetUser));
        assertTrue(policy.canWrite(manager, manager));

        ForbiddenException otherMgr = assertThrows(ForbiddenException.class,
                () -> policy.assertCanWrite(manager, otherManager));
        assertEquals("TARGET_PROTECTED", otherMgr.getErrorCode());

        ForbiddenException admin = assertThrows(ForbiddenException.class,
                () -> policy.assertCanWrite(manager, targetAdmin));
        assertEquals("TARGET_PROTECTED", admin.getErrorCode());

        ForbiddenException system = assertThrows(ForbiddenException.class,
                () -> policy.assertCanWrite(manager, targetSystem));
        assertEquals("TARGET_PROTECTED", system.getErrorCode());
        assertFalse(policy.canWrite(manager, otherManager));
    }

    @Test
    @DisplayName("ADMIN lê e escreve em qualquer um do tenant, inclusive ADMIN e SYSTEM")
    void adminCanReadAndWriteAnyoneInTenant() {
        User admin = user();
        User targetUser = user();
        User targetManager = user();
        User targetAdmin = user();
        User targetSystem = user();
        stubRoles(admin, SystemRoleNames.ROLE_ADMIN);
        stubRoles(targetUser, SystemRoleNames.ROLE_USER);
        stubRoles(targetManager, SystemRoleNames.ROLE_MANAGER);
        stubRoles(targetAdmin, SystemRoleNames.ROLE_ADMIN);
        stubRoles(targetSystem, SystemRoleNames.ROLE_SYSTEM);

        assertDoesNotThrow(() -> policy.assertCanRead(admin, targetUser));
        assertDoesNotThrow(() -> policy.assertCanWrite(admin, targetManager));
        assertDoesNotThrow(() -> policy.assertCanWrite(admin, targetAdmin));
        assertDoesNotThrow(() -> policy.assertCanWrite(admin, targetSystem));
        assertTrue(policy.canWrite(admin, targetAdmin));
        assertDoesNotThrow(() -> policy.assertCanListTenant(admin));
    }

    @Test
    @DisplayName("SYSTEM lê e escreve em qualquer um do tenant")
    void systemCanAccessAnyoneInTenant() {
        User system = user();
        User targetAdmin = user();
        stubRoles(system, SystemRoleNames.ROLE_SYSTEM);
        stubRoles(targetAdmin, SystemRoleNames.ROLE_ADMIN);

        assertDoesNotThrow(() -> policy.assertCanRead(system, targetAdmin));
        assertDoesNotThrow(() -> policy.assertCanWrite(system, targetAdmin));
        assertDoesNotThrow(() -> policy.assertCanListTenant(system));
    }

    @Test
    @DisplayName("Ator de outro tenant recebe TENANT_MISMATCH")
    void differentCompanyIsRejected() {
        User actor = user();
        User target = UserTestBuilder.builder()
                .withId(UUID.randomUUID())
                .withCompanyId(UUID.randomUUID())
                .withTenantId(UUID.randomUUID())
                .buildDomain();
        stubRoles(actor, SystemRoleNames.ROLE_ADMIN);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> policy.assertCanRead(actor, target));
        assertEquals("TENANT_MISMATCH", ex.getErrorCode());
        assertFalse(policy.canWrite(actor, target));
    }

    @Test
    @DisplayName("USER+MANAGER no alvo conta como MANAGER na escrita")
    void highestRankWinsOnTarget() {
        User manager = user();
        User target = user();
        stubRoles(manager, SystemRoleNames.ROLE_MANAGER);
        stubRoles(target, List.of(SystemRoleNames.ROLE_USER, SystemRoleNames.ROLE_MANAGER));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> policy.assertCanWrite(manager, target));
        assertEquals("TARGET_PROTECTED", ex.getErrorCode());
        assertDoesNotThrow(() -> policy.assertCanRead(manager, target));
    }

    private User user() {
        return UserTestBuilder.builder()
                .withId(UUID.randomUUID())
                .withCompanyId(companyId)
                .withTenantId(companyId)
                .buildDomain();
    }

    private void stubRoles(User user, String roleName) {
        stubRoles(user, List.of(roleName));
    }

    private void stubRoles(User user, List<String> roleNames) {
        List<UserRole> assignments = roleNames.stream()
                .map(name -> {
                    Role role = RoleTestBuilder.builder()
                            .withCompanyId(companyId)
                            .withName(name)
                            .buildDomain();
                    role.setAuthorities(new HashSet<>());
                    lenient().when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
                    return UserRole.assign(user.getId(), role.getId());
                })
                .toList();
        lenient().when(userRoleRepository.findByUserId(user.getId())).thenReturn(assignments);
    }
}
