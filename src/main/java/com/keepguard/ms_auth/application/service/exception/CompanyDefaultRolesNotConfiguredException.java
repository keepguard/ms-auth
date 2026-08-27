package com.keepguard.ms_auth.application.service.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class CompanyDefaultRolesNotConfiguredException extends RuntimeException {

    public static final String ERROR_CODE = "COMPANY_DEFAULT_ROLES_NOT_CONFIGURED";

    private final UUID companyId;

    public CompanyDefaultRolesNotConfiguredException(UUID companyId) {
        super("Company não possui roles default configuradas: " + companyId);
        this.companyId = companyId;
        MDC.put("errorCode", ERROR_CODE);
        MDC.put("companyId", String.valueOf(companyId));
        log.warn("Company sem roles default: {}", companyId);
        MDC.remove("errorCode");
        MDC.remove("companyId");
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public Map<String, Object> getContext() {
        return companyId == null ? Map.of() : Map.of("companyId", companyId);
    }
}
