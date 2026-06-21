package com.keepguard.ms_auth.application.service.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

@Slf4j
public class AlreadyExistsException extends RuntimeException {
    
    private final String errorCode;
    private final Map<String, Object> context;
    
    public AlreadyExistsException(String message) {
        super(message);
        this.errorCode = "RESOURCE_ALREADY_EXISTS";
        this.context = Map.of();
        logStructuredError();
    }
    
    public AlreadyExistsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = Map.of();
        logStructuredError();
    }
    
    public AlreadyExistsException(String message, String errorCode, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }
    
    private void logStructuredError() {
        // Adiciona contexto ao MDC
        MDC.put("errorCode", errorCode);
        MDC.put("exceptionType", this.getClass().getSimpleName());
        
        // Adiciona contexto adicional se fornecido
        if (context != null) {
            context.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
        }
        
        // Log estruturado
        log.warn("Recurso já existe: {} - Código: {} - Contexto: {}", 
                getMessage(), errorCode, context);
        
        // Remove contexto do MDC
        MDC.remove("errorCode");
        MDC.remove("exceptionType");
        if (context != null) {
            context.keySet().forEach(MDC::remove);
        }
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
}