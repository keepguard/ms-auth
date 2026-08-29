package com.keepguard.ms_auth.infrastructure.audit;

import org.slf4j.MDC;

public final class AuditMdc {

    private AuditMdc() {
    }

    public static void bind(String codeUser, String tenantOrCompany, String deviceId) {
        if (notBlank(codeUser)) {
            MDC.put("codeUser", codeUser);
        }
        if (notBlank(tenantOrCompany)) {
            MDC.put("tenantId", tenantOrCompany);
            MDC.put("companyId", tenantOrCompany);
        }
        if (notBlank(deviceId)) {
            MDC.put("deviceId", deviceId);
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
