package com.keepguard.ms_auth.adapters.in.rest.role;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_auth.application.dto.role.ProvisionCompanyRolesView;
import com.keepguard.ms_auth.application.port.in.CompanyRoleProvisionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Company Roles", description = "Provisionamento de roles por empresa")
public class CompanyRoleProvisionController {

    private final CompanyRoleProvisionPort companyRoleProvisionPort;

    @PostMapping("/{companyId}/roles/provision")
    @Operation(summary = "Provisionar roles da company", description = "Clona ROLE_ADMIN, ROLE_MANAGER e ROLE_USER para a company. Idempotente.")
    @MetricsEndpoint(endpoint = "company_roles_provision", operation = "provisionar roles da company")
    public ResponseEntity<ProvisionCompanyRolesView> provision(
            @Parameter(description = "ID da company", required = true)
            @PathVariable UUID companyId) {
        log.info("Provisionando roles para company {}", companyId);
        ProvisionCompanyRolesView view = companyRoleProvisionPort.provision(companyId);
        HttpStatus status = view.alreadyProvisioned() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(view);
    }
}
