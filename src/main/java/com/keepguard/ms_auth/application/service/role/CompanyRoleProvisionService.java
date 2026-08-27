package com.keepguard.ms_auth.application.service.role;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_auth.application.dto.role.ProvisionCompanyRolesView;
import com.keepguard.ms_auth.application.port.in.CompanyRoleProvisionPort;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.port.out.persistence.CompanyRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.role.CompanyRole;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyRoleProvisionService implements CompanyRoleProvisionPort {

    private final RoleRepositoryPort roleRepository;
    private final CompanyRoleRepositoryPort companyRoleRepository;
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

        List<String> createdNames = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (String roleName : SystemRoleNames.PROVISIONED) {
            Role template = roleRepository.findByCompanyIdIsNullAndName(roleName)
                    .orElseThrow(() -> new NotFoundException("Template de role não encontrado: " + roleName));

            Role clone = Role.builder()
                    .name(template.getName())
                    .description(template.getDescription())
                    .companyId(companyId)
                    .isSystem(true)
                    .authorities(new HashSet<>())
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
}
