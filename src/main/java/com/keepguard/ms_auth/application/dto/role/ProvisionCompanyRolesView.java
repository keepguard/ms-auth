package com.keepguard.ms_auth.application.dto.role;

import java.util.List;
import java.util.UUID;

public record ProvisionCompanyRolesView(
    UUID companyId,
    boolean alreadyProvisioned,
    List<String> roleNames
) {}
