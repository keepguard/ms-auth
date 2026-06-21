package com.keepguard.ms_auth.adapters.out.feign;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommunicationClientConfig {

    @Bean
    public Logger.Level communicationClientLoggerLevel() {
        return Logger.Level.FULL;
    }
}