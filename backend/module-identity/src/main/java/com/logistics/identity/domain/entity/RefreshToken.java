package com.logistics.identity.domain.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class RefreshToken {

    private final UUID id;
    private final UUID sessionId;

    /*
     * This is NEVER the raw refresh token.
     * It represents the securely stored hash.
     */
    private final String tokenHash;

    private final Instant createdAt;
    private final Instant expiresAt;

    private Instant revokedAt;

    private UUID replacedByTokenId;

    private RefreshToken(
            UUID id,
            UUID sessionId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt,
            Instant revokedAt,
            UUID replacedByTokenId) {
        this.id = Objects.requireNonNull(id, "Refresh token ID cannot be null");

        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");

        this.tokenHash = requireText(tokenHash, "Token hash");

        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");

        this.expiresAt = Objects.requireNonNull(expiresAt, "Expires at cannot be null");

        this.revokedAt = revokedAt;
        this.replacedByTokenId = replacedByTokenId;

        if (expiresAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Refresh token expiration cannot be before creation");
        }
    }

    public static RefreshToken create(UUID id, UUID sessionId, String tokenHash, Instant createdAt, Instant expiresAt) {
        return new RefreshToken(id, sessionId, tokenHash, createdAt, expiresAt, null, null);
    }

    public static RefreshToken reconstitute(
            UUID id,
            UUID sessionId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt,
            Instant revokedAt,
            UUID replacedByTokenId) {
        return new RefreshToken(id, sessionId, tokenHash, createdAt, expiresAt, revokedAt, replacedByTokenId);
    }

    public UUID id() {
        return id;
    }

    public UUID sessionId() {
        return sessionId;
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

    public Instant revokedAt() {
        return revokedAt;
    }

    public UUID replacedByTokenId() {
        return replacedByTokenId;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isReplaced() {
        return replacedByTokenId != null;
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "Current time cannot be null");

        return !now.isBefore(expiresAt);
    }

    public boolean isActive(Instant now) {
        Objects.requireNonNull(now, "Current time cannot be null");

        return !isRevoked() && !isReplaced() && !isExpired(now);
    }

    public void revoke(Instant revokedAt) {
        Objects.requireNonNull(revokedAt, "Revoked at cannot be null");

        if (this.revokedAt == null) {
            this.revokedAt = revokedAt;
        }
    }

    public void replaceWith(UUID replacementTokenId, Instant replacedAt) {
        Objects.requireNonNull(replacementTokenId, "Replacement token ID cannot be null");

        Objects.requireNonNull(replacedAt, "Replacement time cannot be null");

        if (this.replacedByTokenId != null) {
            throw new IllegalStateException("Refresh token has already been replaced");
        }

        this.replacedByTokenId = replacementTokenId;

        if (this.revokedAt == null) {
            this.revokedAt = replacedAt;
        }
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }

        return value;
    }
}
