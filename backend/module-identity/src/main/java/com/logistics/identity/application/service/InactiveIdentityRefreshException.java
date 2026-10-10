package com.logistics.identity.application.service;

public class InactiveIdentityRefreshException extends IllegalArgumentException {

    public InactiveIdentityRefreshException(String message) {
        super(message);
    }
}
