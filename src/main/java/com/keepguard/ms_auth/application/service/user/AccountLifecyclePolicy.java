package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.ForbiddenException;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.user.UserRole;
import com.keepguard.ms_auth.domain.enums.AccountLifecycleAction;
import com.keepguard.ms_auth.domain.enums.AccountLifecycleRank;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountLifecyclePolicy {

    private final UserRoleRepositoryPort userRoleRepository;
    private final RoleRepositoryPort roleRepository;
    private final UserRepositoryPort userRepository;

    public void assertAllowed(User actor, User target, AccountLifecycleAction action) {
        if (actor == null || target == null || action == null) {
            throw new ForbiddenException("Não foi possível autorizar a operação.", "LIFECYCLE_FORBIDDEN");
        }

        if (!Objects.equals(actor.getCompanyId(), target.getCompanyId())) {
            throw new ForbiddenException("Usuário alvo pertence a outro tenant.", "TENANT_MISMATCH");
        }
        if (!Objects.equals(actor.getCompanyId(), target.getCompanyId())) {
            throw new ForbiddenException("Usuário alvo pertence a outra empresa.", "COMPANY_MISMATCH");
        }

        AccountLifecycleRank targetRank = rankOf(target);
        AccountLifecycleRank actorRank = rankOf(actor);

        if (targetRank == AccountLifecycleRank.ADMIN || targetRank == AccountLifecycleRank.SYSTEM) {
            throw new ForbiddenException("Admin e system não podem ser alvo desta operação.", "TARGET_PROTECTED");
        }
        if (isLastAdmin(target, targetRank)) {
            throw new ForbiddenException("O último admin da empresa não pode ser alvo desta operação.", "LAST_ADMIN");
        }

        Set<String> actorAuthorities = authoritiesOf(actor);
        boolean self = Objects.equals(actor.getId(), target.getId());

        if (self) {
            if (actorRank != AccountLifecycleRank.USER) {
                throw new ForbiddenException("Apenas ROLE_USER pode executar esta ação na própria conta.", "SELF_ACTION_FORBIDDEN");
            }
            requireAuthority(actorAuthorities, action.userAuthority());
            return;
        }

        if (actorRank == AccountLifecycleRank.USER) {
            throw new ForbiddenException("ROLE_USER só pode atuar na própria conta.", "SELF_ONLY");
        }

        if (actorRank == AccountLifecycleRank.MANAGER || actorRank == AccountLifecycleRank.ADMIN) {
            if (targetRank == AccountLifecycleRank.USER) {
                requireAuthority(actorAuthorities, action.userAuthority());
                return;
            }
            if (targetRank == AccountLifecycleRank.MANAGER) {
                requireAuthority(actorAuthorities, action.managerAuthority());
                return;
            }
        }

        throw new ForbiddenException("Operação não permitida para o perfil do ator.", "LIFECYCLE_FORBIDDEN");
    }

    AccountLifecycleRank rankOf(User user) {
        return loadRoles(user.getId()).stream()
                .map(Role::getName)
                .map(AccountLifecycleRank::fromRoleName)
                .reduce(AccountLifecycleRank.NONE, AccountLifecycleRank::max);
    }

    private Set<String> authoritiesOf(User user) {
        Set<String> names = new HashSet<>();
        for (Role role : loadRoles(user.getId())) {
            if (role.getAuthorities() == null) {
                continue;
            }
            for (Authority authority : role.getAuthorities()) {
                if (authority.getName() != null) {
                    names.add(authority.getName());
                }
            }
        }
        return names;
    }

    private List<Role> loadRoles(UUID userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .map(roleRepository::findById)
                .filter(optional -> optional.isPresent())
                .map(optional -> optional.get())
                .toList();
    }

    private boolean isLastAdmin(User target, AccountLifecycleRank targetRank) {
        if (targetRank != AccountLifecycleRank.ADMIN || target.getCompanyId() == null) {
            return false;
        }
        Role adminRole = roleRepository.findByCompanyIdAndName(target.getCompanyId(), SystemRoleNames.ROLE_ADMIN)
                .orElse(null);
        if (adminRole == null) {
            return false;
        }
        long activeAdmins = userRoleRepository.findByRoleId(adminRole.getId()).stream()
                .map(UserRole::getUserId)
                .map(userRepository::findById)
                .filter(optional -> optional.isPresent())
                .map(optional -> optional.get())
                .filter(user -> Objects.equals(user.getCompanyId(), target.getCompanyId()))
                .filter(user -> user.getStatus() != UserStatus.DELETED)
                .count();
        return activeAdmins <= 1;
    }

    private void requireAuthority(Set<String> actorAuthorities, String required) {
        if (required == null || !actorAuthorities.contains(required)) {
            throw new ForbiddenException("Authority necessária ausente: " + required, "MISSING_AUTHORITY");
        }
    }
}
