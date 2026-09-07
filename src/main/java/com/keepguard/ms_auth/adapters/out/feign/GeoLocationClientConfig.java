package com.keepguard.ms_auth.adapters.out.feign;

import feign.Request;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

public class GeoLocationClientConfig {

    @Bean
    public Request.Options geoLocationRequestOptions() {
        return new Request.Options(Duration.ofSeconds(2), Duration.ofSeconds(3), true);
    }
}
