package com.logistics.identity.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    @Test
    void shouldCreateActiveRefreshToken() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T12:00:00Z");

        RefreshToken token =
                RefreshToken.create(UUID.randomUUID(), UUID.randomUUID(), "hashed-token", createdAt, expiresAt);

        assertFalse(token.isRevoked());
        assertFalse(token.isReplaced());
        assertFalse(token.isExpired(createdAt));
        assertTrue(token.isActive(createdAt));
    }

    @Test
    void shouldDetectExpiredRefreshToken() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T12:00:00Z");
        Instant afterExpiration = Instant.parse("2026-09-22T12:00:01Z");

        RefreshToken token =
                RefreshToken.create(UUID.randomUUID(), UUID.randomUUID(), "hashed-token", createdAt, expiresAt);

        assertTrue(token.isExpired(afterExpiration));
        assertFalse(token.isActive(afterExpiration));
    }

    @Test
    void shouldRevokeRefreshToken() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T12:00:00Z");
        Instant revokedAt = Instant.parse("2026-09-22T10:30:00Z");

        RefreshToken token =
                RefreshToken.create(UUID.randomUUID(), UUID.randomUUID(), "hashed-token", createdAt, expiresAt);

        token.revoke(revokedAt);

        assertTrue(token.isRevoked());
        assertEquals(revokedAt, token.revokedAt());
        assertFalse(token.isActive(revokedAt));
    }

    @Test
    void shouldReplaceRefreshToken() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T12:00:00Z");
        Instant replacedAt = Instant.parse("2026-09-22T10:30:00Z");

        UUID replacementTokenId = UUID.randomUUID();

        RefreshToken token =
                RefreshToken.create(UUID.randomUUID(), UUID.randomUUID(), "hashed-token", createdAt, expiresAt);

        token.replaceWith(replacementTokenId, replacedAt);

        assertTrue(token.isReplaced());
        assertTrue(token.isRevoked());
        assertEquals(replacementTokenId, token.replacedByTokenId());
        assertEquals(replacedAt, token.revokedAt());
        assertFalse(token.isActive(replacedAt));
    }

    @Test
    void shouldNotReplaceRefreshTokenTwice() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T12:00:00Z");
        Instant replacedAt = Instant.parse("2026-09-22T10:30:00Z");

        RefreshToken token =
                RefreshToken.create(UUID.randomUUID(), UUID.randomUUID(), "hashed-token", createdAt, expiresAt);

        token.replaceWith(UUID.randomUUID(), replacedAt);

        assertThrows(
                IllegalStateException.class, () -> token.replaceWith(UUID.randomUUID(), replacedAt.plusSeconds(1)));
    }

    @Test
    void shouldRejectExpirationBeforeCreation() {
        Instant createdAt = Instant.parse("2026-09-22T12:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T10:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> RefreshToken.create(UUID.randomUUID(), UUID.randomUUID(), "hashed-token", createdAt, expiresAt));
    }

    @Test
    void shouldRejectBlankTokenHash() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T12:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> RefreshToken.create(UUID.randomUUID(), UUID.randomUUID(), "   ", createdAt, expiresAt));
    }
}
