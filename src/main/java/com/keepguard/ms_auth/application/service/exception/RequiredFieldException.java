package com.keepguard.ms_auth.application.service.exception;

public class RequiredFieldException extends RuntimeException {
    public RequiredFieldException(String message) {
        super(message);
    }
}