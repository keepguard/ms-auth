package com.keepguard.ms_auth.infrastructure.config;

import com.keepguard.ms_auth.domain.entity.role.SystemServiceRoleNames;
import com.keepguard.ms_auth.infrastructure.persistence.entity.AuthorityJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.spring.AuthoritySpringRepository;
import com.keepguard.ms_auth.infrastructure.persistence.spring.RoleSpringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class ServiceRoleSeeder implements CommandLineRunner {

    private final RoleSpringRepository roleRepository;
    private final AuthoritySpringRepository authorityRepository;

    @Override
    public void run(String... args) {
        log.info("Iniciando verificação de service roles globais no banco de dados...");

        for (String roleName : SystemServiceRoleNames.SERVICE_TEMPLATES) {
            RoleJpaEntity role = roleRepository.findByCompanyIdIsNullAndName(roleName).orElseGet(() -> {
                log.info("Service role {} não encontrada. Criando...", roleName);
                RoleJpaEntity created = RoleJpaEntity.builder()
                        .name(roleName)
                        .description(SystemServiceRoleNames.descriptionFor(roleName))
                        .companyId(null)
                        .isSystem(true)
                        .authorities(new HashSet<>())
                        .build();
                RoleJpaEntity saved = roleRepository.save(created);
                log.info("Service role {} criada com sucesso.", roleName);
                return saved;
            });
            syncAuthorities(role, roleName);
        }
    }

    private void syncAuthorities(RoleJpaEntity role, String roleName) {
        Set<AuthorityJpaEntity> current = role.getAuthorities() == null ? new HashSet<>() : role.getAuthorities();
        boolean changed = false;
        for (String authorityName : SystemServiceRoleNames.authoritiesFor(roleName)) {
            AuthorityJpaEntity authority = authorityRepository.findByName(authorityName)
                    .orElseThrow(() -> new IllegalStateException(
                            "Authority global não encontrada para service role: " + authorityName));
            boolean alreadyLinked = current.stream().anyMatch(item -> authorityName.equals(item.getName()));
            if (!alreadyLinked) {
                current.add(authority);
                changed = true;
                log.info("Associando authority {} à service role {}", authorityName, roleName);
            }
        }
        if (changed) {
            role.setAuthorities(current);
            roleRepository.save(role);
        }
    }
}
