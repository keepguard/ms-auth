package com.keepguard.ms_auth.application.service.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super("Recurso não encontrado.");
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}