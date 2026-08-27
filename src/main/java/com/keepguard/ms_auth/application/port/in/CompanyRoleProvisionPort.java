package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.role.ProvisionCompanyRolesView;

import java.util.UUID;

public interface CompanyRoleProvisionPort {
    ProvisionCompanyRolesView provision(UUID companyId);
}
