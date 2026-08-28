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
        log.info("Iniciando verificação de templates de Authorities no banco de dados...");

        for (String name : SystemAuthorityNames.TEMPLATES) {
            authorityRepository.findByCompanyIdIsNullAndName(name).ifPresentOrElse(
                    authority -> log.debug("Template de authority {} já existe no banco de dados.", name),
                    () -> {
                        log.info("Template de authority {} não encontrado. Criando...", name);
                        AuthorityJpaEntity created = AuthorityJpaEntity.builder()
                                .name(name)
                                .description("Authority padrão KeepGuard: " + name)
                                .companyId(null)
                                .build();
                        authorityRepository.save(created);
                        log.info("Template de authority {} criado com sucesso.", name);
                    }
            );
        }
    }
}
