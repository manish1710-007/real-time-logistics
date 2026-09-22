package com.logistics.identity.domain.entity;

import com.logistics.identity.domain.valueobject.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class UserSession {

    private final UUID id;
    private final UserId userId;

    private final Instant createdAt;
    private Instant lastSeenAt;
    private final Instant expiresAt;

    private Instant revokedAt;

    private final String ipAddress;
    private final String userAgent;

    private UserSession(
            UUID id,
            UserId userId,
            Instant createdAt,
            Instant lastSeenAt,
            Instant expiresAt,
            Instant revokedAt,
            String ipAddress,
            String userAgent) {
        this.id = Objects.requireNonNull(id, "Session ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");

        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");

        this.lastSeenAt = Objects.requireNonNull(lastSeenAt, "Last seen at cannot be null");

        this.expiresAt = Objects.requireNonNull(expiresAt, "Expires at cannot be null");

        this.revokedAt = revokedAt;

        this.ipAddress = normalizeOptionalText(ipAddress);
        this.userAgent = normalizeOptionalText(userAgent);

        if (expiresAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Session expiration cannot be before creation");
        }
    }

    public static UserSession create(
            UUID id, UserId userId, Instant createdAt, Instant expiresAt, String ipAddress, String userAgent) {
        Objects.requireNonNull(createdAt, "Created at cannot be null");

        return new UserSession(id, userId, createdAt, createdAt, expiresAt, null, ipAddress, userAgent);
    }

    public static UserSession reconstitute(
            UUID id,
            UserId userId,
            Instant createdAt,
            Instant lastSeenAt,
            Instant expiresAt,
            Instant revokedAt,
            String ipAddress,
            String userAgent) {
        return new UserSession(id, userId, createdAt, lastSeenAt, expiresAt, revokedAt, ipAddress, userAgent);
    }

    public UUID id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant lastSeenAt() {
        return lastSeenAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public String ipAddress() {
        return ipAddress;
    }

    public String userAgent() {
        return userAgent;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "Current time cannot be null");
        return !now.isBefore(expiresAt);
    }

    public boolean isActive(Instant now) {
        Objects.requireNonNull(now, "Current time cannot be null");

        return !isRevoked() && !isExpired(now);
    }

    public void touch(Instant seenAt) {
        Objects.requireNonNull(seenAt, "Last seen time cannot be null");

        if (seenAt.isBefore(lastSeenAt)) {
            throw new IllegalArgumentException("Last seen time cannot be before the current last seen time");
        }

        this.lastSeenAt = seenAt;
    }

    public void revoke(Instant revokedAt) {
        Objects.requireNonNull(revokedAt, "Revoked at cannot be null");

        if (this.revokedAt == null) {
            this.revokedAt = revokedAt;
        }
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}
