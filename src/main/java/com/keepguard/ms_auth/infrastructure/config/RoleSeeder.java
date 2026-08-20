package com.keepguard.ms_auth.infrastructure.config;

import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.spring.RoleSpringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements CommandLineRunner {

    private final RoleSpringRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando verificação de Roles no banco de dados...");

        List<String> defaultRoles = Arrays.asList("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER");

        for (String roleName : defaultRoles) {
            roleRepository.findByName(roleName).ifPresentOrElse(
                role -> log.debug("Role {} já existe no banco de dados.", roleName),
                () -> {
                    log.info("Role {} não encontrada. Criando nova role...", roleName);
                    RoleJpaEntity newRole = RoleJpaEntity.builder()
                        .name(roleName)
                        .description("Role padrão: " + roleName)
                        .build();
                    roleRepository.save(newRole);
                    log.info("Role {} criada com sucesso.", roleName);
                }
            );
        }
    }
}
