package com.logistics.identity.application.service;

public class RefreshTokenReuseDetectedException extends RuntimeException {

    public RefreshTokenReuseDetectedException() {
        super("Refresh token reuse detected");
    }
}
