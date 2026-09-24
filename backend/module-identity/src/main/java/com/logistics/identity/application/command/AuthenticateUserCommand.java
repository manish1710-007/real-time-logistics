package com.logistics.identity.application.command;

import com.logistics.identity.domain.valueobject.TenantId;

public record AuthenticateUserCommand(TenantId tenantId, String email, String password) {
    public AuthenticateUserCommand {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        email = email.trim().toLowerCase();
    }
}
