package com.logistics.identity.application.dto;

import java.time.Instant;

public record RefreshTokenResult(
        String accessToken, String refreshToken, Instant accessTokenExpiresAt, Instant refreshTokenExpiresAt) {}
