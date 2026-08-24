package com.keepguard.ms_auth.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
    name = "communication-service",
    url = "${feign.client.communication.url:http://localhost:8082}",
    configuration = CommunicationClientConfig.class
)
public interface CommunicationClient {

    @GetMapping("/api/v1/health")
    String health(@RequestHeader("X-Tenant-Id") String tenantIdHeader);

    @GetMapping("/api/v1/communication/test")
    String testCommunication(@RequestHeader("X-Tenant-Id") String tenantIdHeader);

    @PostMapping("/api/v1/messages/send")
    Map<String, Object> sendMessage(
        @RequestBody Map<String, Object> request,
        @RequestHeader("X-Tenant-Id") String tenantIdHeader
    );
}