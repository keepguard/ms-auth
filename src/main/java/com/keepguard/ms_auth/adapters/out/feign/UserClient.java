package com.keepguard.ms_auth.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.UUID;

/**
 * Feign Client para comunicação com ms-user
 * Usado para buscar display_handle do usuário para incluir no JWT
 */
@FeignClient(
    name = "user-service",
    url = "${feign.client.user.url:http://localhost:8580}",
    configuration = UserClientConfig.class
)
public interface UserClient {

    @GetMapping("/internal/v1/users/code/{codeUser}")
    Map<String, Object> getUserByCode(
        @PathVariable("codeUser") UUID codeUser,
        @RequestHeader("X-Application") String xApplication
    );
}
