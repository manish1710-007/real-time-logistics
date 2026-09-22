package com.logistics.identity.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.domain.valueobject.UserStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldCreateUserInInvitedStatus() {
        TenantId tenantId = TenantId.generate();
        UserId userId = UserId.generate();

        User user =
                User.create(userId, tenantId, "john@example.com", "hashed-password", "John", "Doe", "+919876543210");

        assertEquals(userId, user.id());
        assertEquals(tenantId, user.tenantId());
        assertEquals("john@example.com", user.email());
        assertEquals("hashed-password", user.passwordHash());
        assertEquals("John", user.firstName());
        assertEquals("Doe", user.lastName());
        assertEquals("+919876543210", user.phone());
        assertEquals(UserStatus.INVITED, user.status());
        assertFalse(user.emailVerified());
        assertNull(user.lastLoginAt());
        assertEquals(0, user.version());
        assertNotNull(user.createdAt());
        assertNotNull(user.updatedAt());
    }

    @Test
    void shouldRejectNullUserId() {
        TenantId tenantId = TenantId.generate();

        assertThrows(
                NullPointerException.class,
                () -> User.create(null, tenantId, "john@example.com", "hashed-password", "John", "Doe", null));
    }

    @Test
    void shouldRejectNullTenantId() {
        UserId userId = UserId.generate();

        assertThrows(
                NullPointerException.class,
                () -> User.create(userId, null, "john@example.com", "hashed-password", "John", "Doe", null));
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.create(
                        UserId.generate(), TenantId.generate(), "   ", "hashed-password", "John", "Doe", null));
    }

    @Test
    void shouldRejectBlankPasswordHash() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.create(
                        UserId.generate(), TenantId.generate(), "john@example.com", "   ", "John", "Doe", null));
    }

    @Test
    void shouldRejectBlankFirstName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.create(
                        UserId.generate(),
                        TenantId.generate(),
                        "john@example.com",
                        "hashed-password",
                        "   ",
                        "Doe",
                        null));
    }

    @Test
    void shouldRejectBlankLastName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.create(
                        UserId.generate(),
                        TenantId.generate(),
                        "john@example.com",
                        "hashed-password",
                        "John",
                        "   ",
                        null));
    }

    @Test
    void shouldNormalizeOptionalPhone() {
        User user = User.create(
                UserId.generate(), TenantId.generate(), "john@example.com", "hashed-password", "John", "Doe", "   ");

        assertNull(user.phone());
    }

    @Test
    void shouldChangeEmail() {
        User user = createUser();

        user.changeEmail("new@example.com");

        assertEquals("new@example.com", user.email());
    }

    @Test
    void shouldChangeName() {
        User user = createUser();

        user.changeName("Jane", "Smith");

        assertEquals("Jane", user.firstName());
        assertEquals("Smith", user.lastName());
    }

    @Test
    void shouldChangePhone() {
        User user = createUser();

        user.changePhone("+911234567890");

        assertEquals("+911234567890", user.phone());
    }

    @Test
    void shouldUpdatePasswordHash() {
        User user = createUser();

        user.updatePasswordHash("new-hashed-password");

        assertEquals("new-hashed-password", user.passwordHash());
    }

    @Test
    void shouldVerifyEmail() {
        User user = createUser();

        assertFalse(user.emailVerified());

        user.verifyEmail();

        assertTrue(user.emailVerified());
    }

    @Test
    void shouldRecordLogin() {
        User user = createUser();
        Instant loginTime = Instant.parse("2026-09-18T00:00:00Z");

        user.recordLogin(loginTime);

        assertEquals(loginTime, user.lastLoginAt());
    }

    @Test
    void shouldChangeLifecycleStatus() {
        User user = createUser();

        user.activate();
        assertEquals(UserStatus.ACTIVE, user.status());

        user.suspend();
        assertEquals(UserStatus.SUSPENDED, user.status());

        user.lock();
        assertEquals(UserStatus.LOCKED, user.status());

        user.disable();
        assertEquals(UserStatus.DISABLED, user.status());

        user.markDeleted();
        assertEquals(UserStatus.DELETED, user.status());
    }

    @Test
    void shouldReconstituteExistingUser() {
        UserId userId = UserId.generate();
        TenantId tenantId = TenantId.generate();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        Instant lastLoginAt = Instant.parse("2026-01-03T00:00:00Z");

        User user = User.reconstitute(
                userId,
                tenantId,
                "john@example.com",
                "hashed-password",
                "John",
                "Doe",
                "+919876543210",
                UserStatus.ACTIVE,
                true,
                lastLoginAt,
                createdAt,
                updatedAt,
                5);

        assertEquals(userId, user.id());
        assertEquals(tenantId, user.tenantId());
        assertEquals("john@example.com", user.email());
        assertEquals("hashed-password", user.passwordHash());
        assertEquals("John", user.firstName());
        assertEquals("Doe", user.lastName());
        assertEquals("+919876543210", user.phone());
        assertEquals(UserStatus.ACTIVE, user.status());
        assertTrue(user.emailVerified());
        assertEquals(lastLoginAt, user.lastLoginAt());
        assertEquals(createdAt, user.createdAt());
        assertEquals(updatedAt, user.updatedAt());
        assertEquals(5, user.version());
    }

    private User createUser() {
        return User.create(
                UserId.generate(),
                TenantId.generate(),
                "john@example.com",
                "hashed-password",
                "John",
                "Doe",
                "+919876543210");
    }
}
