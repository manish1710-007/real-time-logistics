package com.logistics.identity.application.service;

import java.time.Instant;

public interface PasswordResetEmailSender {

    void sendPasswordResetEmail(
            String recipientEmail,
            String rawToken,
            Instant expiresAt);
    
}