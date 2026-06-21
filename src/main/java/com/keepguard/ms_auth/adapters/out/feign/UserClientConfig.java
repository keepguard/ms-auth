package com.keepguard.ms_auth.adapters.out.feign;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserClientConfig {

    @Bean
    public Logger.Level userClientLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
