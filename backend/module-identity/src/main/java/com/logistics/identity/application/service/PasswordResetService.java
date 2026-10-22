package com.logistics.identity.application.service;

import com.logistics.identity.domain.valueobject.TenantId;

public interface PasswordResetService {

    void requestPasswordReset(TenantId tenantId, String email);

    void resetPassword(String rawToken, String newPassword);
}
