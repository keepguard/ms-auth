package com.keepguard.ms_auth.application.service.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

@Slf4j
public class NotFoundException extends RuntimeException {
    
    private final String errorCode;
    private final Map<String, Object> context;
    
    public NotFoundException(String message) {
        super(message);
        this.errorCode = "RESOURCE_NOT_FOUND";
        this.context = Map.of();
        logStructuredError();
    }
    
    public NotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = Map.of();
        logStructuredError();
    }
    
    public NotFoundException(String message, String errorCode, Map<String, Object> context) {
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
        log.warn("Recurso não encontrado: {} - Código: {} - Contexto: {}", 
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