package com.keepguard.ms_auth.infrastructure.config;

import com.keepguard.ms_auth.infrastructure.interceptor.FeignCorrelationIdInterceptor;
import com.keepguard.ms_auth.infrastructure.interceptor.FeignErrorDecoder;
import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.keepguard.ms_auth.infrastructure.feign")
public class FeignConfig {

    private final FeignCorrelationIdInterceptor correlationIdInterceptor;

    public FeignConfig(FeignCorrelationIdInterceptor correlationIdInterceptor) {
        this.correlationIdInterceptor = correlationIdInterceptor;
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public FeignCorrelationIdInterceptor correlationIdInterceptor() {
        return correlationIdInterceptor;
    }
}