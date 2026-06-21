package com.keepguard.ms_auth.application.service.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Exceção específica para operações de comando (Command) que falharam.
 * Esta exceção deve ser lançada quando operações de escrita/modificação falham
 * devido a problemas de infraestrutura, conectividade, validação ou outros erros técnicos.
 */
@Slf4j
public class CommandOperationException extends RuntimeException {

    private final String errorCode;
    private final String operation;
    private final Map<String, Object> context;

    public CommandOperationException(String message, String operation) {
        super(message);
        this.errorCode = "COMMAND_OPERATION_FAILED";
        this.operation = operation;
        this.context = Map.of();
        logStructuredError();
    }

    public CommandOperationException(String message, String operation, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = Map.of();
        logStructuredError();
    }

    public CommandOperationException(String message, String operation, String errorCode, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    public CommandOperationException(String message, String operation, Throwable cause) {
        super(message, cause);
        this.errorCode = "COMMAND_OPERATION_FAILED";
        this.operation = operation;
        this.context = Map.of();
        logStructuredError();
    }

    public CommandOperationException(String message, String operation, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = Map.of();
        logStructuredError();
    }

    public CommandOperationException(String message, String operation, String errorCode, Map<String, Object> context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    private void logStructuredError() {
        MDC.put("errorCode", errorCode);
        MDC.put("exceptionType", this.getClass().getSimpleName());
        MDC.put("operation", operation);

        if (context != null) {
            context.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
        }

        log.error("Falha na operação de comando: {} - Operação: {} - Código: {} - Contexto: {}",
                getMessage(), operation, errorCode, context);

        MDC.remove("errorCode");
        MDC.remove("exceptionType");
        MDC.remove("operation");
        if (context != null) {
            context.keySet().forEach(MDC::remove);
        }
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getOperation() {
        return operation;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
