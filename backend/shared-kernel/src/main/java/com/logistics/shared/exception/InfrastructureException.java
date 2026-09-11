package com.logistics.shared.exception;

public class InfrastructureException extends BusinessException {
    public InfrastructureException(String message, Throwable cause) {
        super("INFRASTRUCTURE_ERROR", message, cause);
    }
}
