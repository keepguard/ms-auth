package com.keepguard.ms_auth.adapters.out.feign;

import com.keepguard.ms_auth.application.port.out.company.CompanyResolverPort;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyResolverAdapter implements CompanyResolverPort {

    private final CompanyClient companyClient;

    @Override
    public UUID resolveCompanyId(UUID tenantId) {
        if (tenantId == null) {
            throw new NotFoundException("Tenant não informado para resolver a company");
        }
        try {
            Map<String, Object> company = companyClient.getCompanyByTenantId(tenantId.toString());
            if (company == null || company.get("id") == null) {
                throw new NotFoundException("Company não encontrada para tenant: " + tenantId);
            }
            Object id = company.get("id");
            if (id instanceof UUID uuid) {
                return uuid;
            }
            return UUID.fromString(id.toString());
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Falha ao resolver company por tenant {}: {}", tenantId, e.getMessage());
            throw new NotFoundException("Company não encontrada para tenant: " + tenantId);
        }
    }
}
