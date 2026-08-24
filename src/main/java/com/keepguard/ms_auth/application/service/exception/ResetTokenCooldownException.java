package com.keepguard.ms_auth.application.service.exception;

public class ResetTokenCooldownException extends RuntimeException {
    public ResetTokenCooldownException(String message) {
        super(message);
    }
}
