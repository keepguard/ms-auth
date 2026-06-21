package com.keepguard.ms_auth.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "communication-service",
    url = "${feign.client.communication.url:http://localhost:8081}",
    configuration = CommunicationClientConfig.class
)
public interface CommunicationClient {

    @GetMapping("/api/v1/health")
    String health(@RequestHeader("X-Application") String application);

    @GetMapping("/api/v1/communication/test")
    String testCommunication(@RequestHeader("X-Application") String application);
}