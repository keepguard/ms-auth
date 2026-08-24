package com.keepguard.ms_auth.application.service.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

@Slf4j
public class DeviceBlacklistedException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> context;

    public DeviceBlacklistedException(String message) {
        super(message);
        this.errorCode = "DEVICE_BLACKLISTED";
        this.context = Map.of();
        logStructuredError();
    }

    public DeviceBlacklistedException(String message, String errorCode, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    private void logStructuredError() {
        MDC.put("errorCode", errorCode);
        MDC.put("exceptionType", this.getClass().getSimpleName());
        if (context != null) {
            context.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
        }
        log.error("Dispositivo bloqueado na blacklist: {} - Código: {} - Contexto: {}", 
                getMessage(), errorCode, context);
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
