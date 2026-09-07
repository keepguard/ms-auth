package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.role.ProvisionCompanyRolesViewDTO;

import java.util.UUID;

public interface CompanyRoleProvisionPort {
    ProvisionCompanyRolesViewDTO provision(UUID companyId);
}
