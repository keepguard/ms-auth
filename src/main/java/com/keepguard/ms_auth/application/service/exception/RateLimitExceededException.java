package com.keepguard.ms_auth.application.service.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
        log.warn("Rate limit excedido: {}", message);
    }
}
