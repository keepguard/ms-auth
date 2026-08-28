package com.keepguard.ms_auth.application.service.role;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_auth.application.dto.role.ProvisionCompanyRolesView;
import com.keepguard.ms_auth.application.port.in.CompanyRoleProvisionPort;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.port.out.persistence.AuthorityRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.CompanyRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;
import com.keepguard.ms_auth.domain.entity.role.CompanyRole;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyRoleProvisionService implements CompanyRoleProvisionPort {

    private final RoleRepositoryPort roleRepository;
    private final CompanyRoleRepositoryPort companyRoleRepository;
    private final AuthorityRepositoryPort authorityRepository;
    private final MetricsPort metricsPort;

    @Override
    @Transactional
    @LogOperation(
        operation = "PROVISION_COMPANY_ROLES",
        description = "Provisionando roles da company: {companyId}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "COMPANY_ROLE"
    )
    public ProvisionCompanyRolesView provision(UUID companyId) {
        if (companyId == null) {
            throw new NotFoundException("companyId é obrigatório para provisionar roles");
        }

        if (companyRoleRepository.existsByCompanyId(companyId)) {
            log.info("Roles já provisionadas para company {}", companyId);
            List<String> existingNames = roleRepository.findByCompanyId(companyId).stream()
                    .map(Role::getName)
                    .toList();
            return new ProvisionCompanyRolesView(companyId, true, existingNames);
        }

        LocalDateTime now = LocalDateTime.now();
        Map<String, Authority> clonedAuthorities = cloneAuthorities(companyId, now);
        List<String> createdNames = new ArrayList<>();

        for (String roleName : SystemRoleNames.PROVISIONED) {
            Role template = roleRepository.findByCompanyIdIsNullAndName(roleName)
                    .orElseThrow(() -> new NotFoundException("Template de role não encontrado: " + roleName));

            Set<Authority> authorities = new HashSet<>();
            for (String authorityName : SystemAuthorityNames.defaultAuthoritiesForRole(roleName)) {
                Authority cloned = clonedAuthorities.get(authorityName);
                if (cloned == null) {
                    throw new NotFoundException("Authority provisionada não encontrada: " + authorityName);
                }
                authorities.add(cloned);
            }

            Role clone = Role.builder()
                    .name(template.getName())
                    .description(template.getDescription())
                    .companyId(companyId)
                    .isSystem(true)
                    .authorities(authorities)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Role saved = roleRepository.save(clone);
            boolean isUser = SystemRoleNames.ROLE_USER.equals(roleName);
            companyRoleRepository.save(CompanyRole.create(
                    companyId,
                    saved.getId(),
                    true,
                    isUser
            ));
            createdNames.add(saved.getName());
        }

        metricsPort.incrementCounter("company_roles_provisioned_total",
                Map.of("company_id", companyId.toString()));
        log.info("Roles provisionadas para company {}: {}", companyId, createdNames);
        return new ProvisionCompanyRolesView(companyId, false, createdNames);
    }

    private Map<String, Authority> cloneAuthorities(UUID companyId, LocalDateTime now) {
        Map<String, Authority> cloned = new HashMap<>();
        for (String name : SystemAuthorityNames.TEMPLATES) {
            Authority template = authorityRepository.findByCompanyIdIsNullAndName(name)
                    .orElseThrow(() -> new NotFoundException("Template de authority não encontrado: " + name));
            Authority saved = authorityRepository.save(Authority.builder()
                    .name(template.getName())
                    .description(template.getDescription())
                    .companyId(companyId)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            cloned.put(name, saved);
        }
        return cloned;
    }
}
