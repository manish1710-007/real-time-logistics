package com.logistics.identity.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordResetTokenTest {

    private UUID id;
    private UUID userId;
    private Instant createdAt;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        userId = UUID.randomUUID();
        createdAt = Instant.parse("2026-01-01T10:00:00Z");
        expiresAt = createdAt.plusSeconds(900);
    }

    @Test
    void shouldCreateUsableToken() {
        PasswordResetToken token = PasswordResetToken.create(id, userId, "a".repeat(64), createdAt, expiresAt);

        assertTrue(token.isUsableAt(createdAt.plusSeconds(1)));
        assertEquals(userId, token.userId());
    }

    @Test
    void shouldRejectTokenAtExpiryTime() {
        PasswordResetToken token = PasswordResetToken.create(id, userId, "a".repeat(64), createdAt, expiresAt);

        assertFalse(token.isUsableAt(expiresAt));
    }

    @Test
    void shouldRejectConsumedToken() {
        PasswordResetToken token = PasswordResetToken.create(id, userId, "a".repeat(64), createdAt, expiresAt);

        token.consume(createdAt.plusSeconds(10));

        assertFalse(token.isUsableAt(createdAt.plusSeconds(11)));
        assertThrows(IllegalStateException.class, () -> token.consume(createdAt.plusSeconds(12)));
    }

    @Test
    void shouldRejectRevokedToken() {
        PasswordResetToken token = PasswordResetToken.create(id, userId, "a".repeat(64), createdAt, expiresAt);

        token.revoke(createdAt.plusSeconds(10));

        assertFalse(token.isUsableAt(createdAt.plusSeconds(11)));
    }

    @Test
    void shouldRejectExpiryBeforeCreation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordResetToken.create(id, userId, "a".repeat(64), expiresAt, createdAt));
    }

    @Test
    void shouldRejectBlankTokenHash() {
        assertThrows(
                IllegalArgumentException.class, () -> PasswordResetToken.create(id, userId, " ", createdAt, expiresAt));
    }
}
