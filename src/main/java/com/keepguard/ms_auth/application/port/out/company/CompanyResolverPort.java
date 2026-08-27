package com.keepguard.ms_auth.application.port.out.company;

import java.util.UUID;

public interface CompanyResolverPort {
    UUID resolveCompanyId(UUID tenantId);
}
