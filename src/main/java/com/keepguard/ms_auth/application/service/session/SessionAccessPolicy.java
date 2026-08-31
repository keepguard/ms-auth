package com.keepguard.ms_auth.application.service.session;

import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.ForbiddenException;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.user.UserRole;
import com.keepguard.ms_auth.domain.enums.AccountLifecycleRank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionAccessPolicy {

    private final UserRoleRepositoryPort userRoleRepository;
    private final RoleRepositoryPort roleRepository;

    public void assertCanRead(User actor, User target) {
        requireActorAndTarget(actor, target);
        assertSameCompany(actor, target);

        if (isSelf(actor, target)) {
            return;
        }

        AccountLifecycleRank actorRank = rankOf(actor);
        if (actorRank == AccountLifecycleRank.USER || actorRank == AccountLifecycleRank.NONE) {
            throw new ForbiddenException("ROLE_USER só pode consultar a própria conta.", "SELF_ONLY");
        }
        if (isPrivileged(actorRank)) {
            return;
        }
        throw new ForbiddenException("Operação não permitida para o perfil do ator.", "SESSION_FORBIDDEN");
    }

    public void assertCanWrite(User actor, User target) {
        requireActorAndTarget(actor, target);
        assertSameCompany(actor, target);
        if (canWrite(actor, target)) {
            return;
        }
        AccountLifecycleRank actorRank = rankOf(actor);
        if (actorRank == AccountLifecycleRank.USER || actorRank == AccountLifecycleRank.NONE) {
            throw new ForbiddenException("ROLE_USER só pode atuar na própria conta.", "SELF_ONLY");
        }
        throw new ForbiddenException("Alvo protegido para esta operação.", "TARGET_PROTECTED");
    }

    public boolean canWrite(User actor, User target) {
        if (actor == null || target == null) {
            return false;
        }
        if (!Objects.equals(actor.getCompanyId(), target.getCompanyId())) {
            return false;
        }
        if (isSelf(actor, target)) {
            return true;
        }
        AccountLifecycleRank actorRank = rankOf(actor);
        AccountLifecycleRank targetRank = rankOf(target);
        if (actorRank == AccountLifecycleRank.ADMIN || actorRank == AccountLifecycleRank.SYSTEM) {
            return true;
        }
        if (actorRank == AccountLifecycleRank.MANAGER) {
            return targetRank == AccountLifecycleRank.USER;
        }
        return false;
    }

    public void assertCanListTenant(User actor) {
        if (actor == null) {
            throw new ForbiddenException("Não foi possível autorizar a operação.", "SESSION_FORBIDDEN");
        }
        AccountLifecycleRank actorRank = rankOf(actor);
        if (!isPrivileged(actorRank)) {
            throw new ForbiddenException("Acesso à listagem do tenant restrito a gestores e administradores.", "SESSION_FORBIDDEN");
        }
    }

    public AccountLifecycleRank rankOf(User user) {
        return loadRoles(user.getId()).stream()
                .map(Role::getName)
                .map(AccountLifecycleRank::fromRoleName)
                .reduce(AccountLifecycleRank.NONE, AccountLifecycleRank::max);
    }

    private void requireActorAndTarget(User actor, User target) {
        if (actor == null || target == null) {
            throw new ForbiddenException("Não foi possível autorizar a operação.", "SESSION_FORBIDDEN");
        }
    }

    private void assertSameCompany(User actor, User target) {
        if (!Objects.equals(actor.getCompanyId(), target.getCompanyId())) {
            throw new ForbiddenException("Usuário alvo pertence a outro tenant.", "TENANT_MISMATCH");
        }
    }

    private boolean isSelf(User actor, User target) {
        if (Objects.equals(actor.getId(), target.getId())) {
            return true;
        }
        return actor.getCodeUser() != null && Objects.equals(actor.getCodeUser(), target.getCodeUser());
    }

    private boolean isPrivileged(AccountLifecycleRank rank) {
        return rank == AccountLifecycleRank.MANAGER
                || rank == AccountLifecycleRank.ADMIN
                || rank == AccountLifecycleRank.SYSTEM;
    }

    private List<Role> loadRoles(UUID userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .map(roleRepository::findById)
                .filter(optional -> optional.isPresent())
                .map(optional -> optional.get())
                .toList();
    }
}
