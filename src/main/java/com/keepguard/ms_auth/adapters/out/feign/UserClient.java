package com.keepguard.ms_auth.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.UUID;

/**
 * Feign Client para comunicação com ms-user (ex.: telefone no fluxo de MFA).
 */
@FeignClient(
    name = "user-service",
    url = "${USER_SERVICE_URL:http://localhost:8085}",
    configuration = UserClientConfig.class
)
public interface UserClient {

    @GetMapping("/internal/v1/users/code/{codeUser}")
    Map<String, Object> getUserByCode(
        @PathVariable("codeUser") UUID codeUser,
        @RequestHeader("X-Tenant-Id") String tenantIdHeader
    );
}
