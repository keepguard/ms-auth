package com.keepguard.ms_auth.infrastructure.config;

import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;
import com.keepguard.ms_auth.infrastructure.persistence.entity.AuthorityJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.spring.AuthoritySpringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class AuthoritySeeder implements CommandLineRunner {

    private final AuthoritySpringRepository authorityRepository;

    @Override
    public void run(String... args) {
        log.info("Iniciando verificação de Authorities globais no banco de dados...");

        for (String name : SystemAuthorityNames.TEMPLATES) {
            authorityRepository.findByName(name).ifPresentOrElse(
                    authority -> log.debug("Authority {} já existe no banco de dados.", name),
                    () -> {
                        log.info("Authority {} não encontrada. Criando...", name);
                        AuthorityJpaEntity created = AuthorityJpaEntity.builder()
                                .name(name)
                                .description(SystemAuthorityNames.descriptionFor(name))
                                .build();
                        authorityRepository.save(created);
                        log.info("Authority {} criada com sucesso.", name);
                    }
            );
        }
    }
}
