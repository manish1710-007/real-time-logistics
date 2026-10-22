package com.logistics.identity.domain.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PasswordResetToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant consumedAt;
    private Instant revokedAt;

    private PasswordResetToken(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt,
            Instant consumedAt,
            Instant revokedAt) {

        this.id = Objects.requireNonNull(id, "ID is required");
        this.userId = Objects.requireNonNull(userId, "User ID is required");
        this.tokenHash = requireText(tokenHash, "Token hash");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiry is required");
        this.consumedAt = consumedAt;
        this.revokedAt = revokedAt;

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("Expiry must be after creation");
        }
    }

    public static PasswordResetToken create(
            UUID id, UUID userId, String tokenHash, Instant createdAt, Instant expiresAt) {
        return new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, null, null);
    }

    public static PasswordResetToken reconstitute(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt,
            Instant consumedAt,
            Instant revokedAt) {
        return new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, consumedAt, revokedAt);
    }

    public boolean isUsableAt(Instant now) {
        Objects.requireNonNull(now, "Current time is required");
        return consumedAt == null && revokedAt == null && now.isBefore(expiresAt);
    }

    public void consume(Instant now) {
        Objects.requireNonNull(now, "Current time is required");
        if (!isUsableAt(now)) {
            throw new IllegalStateException("Password reset token is not usable");
        }
        consumedAt = now;
    }

    public void revoke(Instant now) {
        Objects.requireNonNull(now, "Current time is required");
        if (consumedAt == null && revokedAt == null) {
            revokedAt = now;
        }
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant consumedAt() {
        return consumedAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }
}
