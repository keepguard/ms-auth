package com.keepguard.ms_auth.infrastructure.config;

import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;
import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.spring.RoleSpringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements CommandLineRunner {

    private final RoleSpringRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Iniciando verificação de templates de Roles no banco de dados...");

        for (String roleName : SystemRoleNames.TEMPLATES) {
            roleRepository.findByCompanyIdIsNullAndName(roleName).ifPresentOrElse(
                role -> log.debug("Template {} já existe no banco de dados.", roleName),
                () -> {
                    log.info("Template {} não encontrado. Criando...", roleName);
                    RoleJpaEntity newRole = RoleJpaEntity.builder()
                        .name(roleName)
                        .description("Role padrão KeepGuard: " + roleName)
                        .companyId(null)
                        .isSystem(true)
                        .build();
                    roleRepository.save(newRole);
                    log.info("Template {} criado com sucesso.", roleName);
                }
            );
        }
    }
}
