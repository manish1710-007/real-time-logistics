package com.logistics.identity.application.command;

import java.util.Objects;

public record RefreshTokenCommand(String refreshToken) {

    public RefreshTokenCommand {
        Objects.requireNonNull(refreshToken, "Refresh token cannot be null");

        if (refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be blank");
        }
    }
}
