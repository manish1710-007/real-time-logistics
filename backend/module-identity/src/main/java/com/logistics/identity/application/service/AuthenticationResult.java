package com.logistics.identity.application.service;

import java.time.Instant;
import java.util.UUID;

public record AuthenticationResult(
        UUID userId,
        UUID tenantId,
        UUID sessionId,
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt) {}
