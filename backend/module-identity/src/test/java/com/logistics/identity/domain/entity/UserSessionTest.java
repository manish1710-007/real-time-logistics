package com.logistics.identity.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.logistics.identity.domain.valueobject.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserSessionTest {

    @Test
    void shouldCreateActiveSession() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T11:00:00Z");

        UserSession session = UserSession.create(
                UUID.randomUUID(), new UserId(UUID.randomUUID()), createdAt, expiresAt, "127.0.0.1", "Test-Agent");

        assertFalse(session.isRevoked());
        assertFalse(session.isExpired(createdAt));
        assertTrue(session.isActive(createdAt));
        assertEquals(createdAt, session.lastSeenAt());
    }

    @Test
    void shouldDetectExpiredSession() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T11:00:00Z");
        Instant afterExpiration = Instant.parse("2026-09-22T11:00:01Z");

        UserSession session =
                UserSession.create(UUID.randomUUID(), new UserId(UUID.randomUUID()), createdAt, expiresAt, null, null);

        assertTrue(session.isExpired(afterExpiration));
        assertFalse(session.isActive(afterExpiration));
    }

    @Test
    void shouldRevokeSession() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T11:00:00Z");
        Instant revokedAt = Instant.parse("2026-09-22T10:30:00Z");

        UserSession session =
                UserSession.create(UUID.randomUUID(), new UserId(UUID.randomUUID()), createdAt, expiresAt, null, null);

        session.revoke(revokedAt);

        assertTrue(session.isRevoked());
        assertEquals(revokedAt, session.revokedAt());
        assertFalse(session.isActive(revokedAt));
    }

    @Test
    void shouldUpdateLastSeenTime() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T11:00:00Z");
        Instant seenAt = Instant.parse("2026-09-22T10:15:00Z");

        UserSession session =
                UserSession.create(UUID.randomUUID(), new UserId(UUID.randomUUID()), createdAt, expiresAt, null, null);

        session.touch(seenAt);

        assertEquals(seenAt, session.lastSeenAt());
    }

    @Test
    void shouldRejectLastSeenTimeBeforeCurrentValue() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T11:00:00Z");
        Instant earlierTime = Instant.parse("2026-09-22T09:59:00Z");

        UserSession session =
                UserSession.create(UUID.randomUUID(), new UserId(UUID.randomUUID()), createdAt, expiresAt, null, null);

        assertThrows(IllegalArgumentException.class, () -> session.touch(earlierTime));
    }

    @Test
    void shouldRejectExpirationBeforeCreation() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T09:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> UserSession.create(
                        UUID.randomUUID(), new UserId(UUID.randomUUID()), createdAt, expiresAt, null, null));
    }

    @Test
    void shouldNormalizeOptionalText() {
        Instant createdAt = Instant.parse("2026-09-22T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-09-22T11:00:00Z");

        UserSession session = UserSession.create(
                UUID.randomUUID(), new UserId(UUID.randomUUID()), createdAt, expiresAt, " 127.0.0.1 ", " Test-Agent ");

        assertEquals("127.0.0.1", session.ipAddress());
        assertEquals("Test-Agent", session.userAgent());
    }
}
