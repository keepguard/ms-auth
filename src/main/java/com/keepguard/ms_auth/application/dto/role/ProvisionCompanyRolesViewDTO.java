package com.keepguard.ms_auth.application.dto.role;

import java.util.List;
import java.util.UUID;

public record ProvisionCompanyRolesViewDTO(
    UUID companyId,
    boolean alreadyProvisioned,
    List<String> roleNames
) {}
