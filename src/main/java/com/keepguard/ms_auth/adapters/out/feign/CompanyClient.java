package com.keepguard.ms_auth.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
    name = "company-service",
    url = "${feign.client.company.url:http://localhost:8583}",
    configuration = UserClientConfig.class
)
public interface CompanyClient {

    @GetMapping("/api/v1/companies/x-tenant-id/{tenantId}")
    Map<String, Object> getCompanyByTenantId(@PathVariable("tenantId") String tenantId);
}
