package com.logistics.shared.exception;

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}
