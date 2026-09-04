package com.logistics.shared.exception;

public class AuthorizationException extends BusinessException {
    public AuthorizationException(String message) {
        super("AUTHORIZATION_FAILED", message);
    }
}
