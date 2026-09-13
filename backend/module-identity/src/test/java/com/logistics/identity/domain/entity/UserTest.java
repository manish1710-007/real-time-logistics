package com.logistics.identity.domain.entity;

import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.domain.valueobject.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserInInvitedStatus() {
        TenantId tenantId = TenantId.generate();
        UserId userId = UserId.generate();

        User user = User.create(
                userId,
                tenantId,
                "john@example.com",
                "John Doe"
        );

        assertEquals(userId, user.id());
        assertEquals(tenantId, user.tenantId());
        assertEquals("john@example.com", user.email());
        assertEquals("John Doe", user.displayName());
        assertEquals(UserStatus.INVITED, user.status());
        assertNotNull(user.createdAt());
        assertNotNull(user.updatedAt());
    }

    @Test
    void shouldRejectNullUserId() {
        TenantId tenantId = TenantId.generate();

        assertThrows(
                NullPointerException.class,
                () -> User.create(
                        null,
                        tenantId,
                        "john@example.com",
                        "John Doe"
                )
        );
    }

    @Test
    void shouldRejectNullTenantId() {
        UserId userId = UserId.generate();

        assertThrows(
                NullPointerException.class,
                () -> User.create(
                        userId,
                        null,
                        "john@example.com",
                        "John Doe"
                )
        );
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.create(
                        UserId.generate(),
                        TenantId.generate(),
                        "   ",
                        "John Doe"
                )
        );
    }

    @Test
    void shouldRejectBlankDisplayName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.create(
                        UserId.generate(),
                        TenantId.generate(),
                        "john@example.com",
                        "   "
                )
        );
    }

    @Test
    void shouldChangeEmail() {
        User user = createUser();

        user.changeEmail("new@example.com");

        assertEquals("new@example.com", user.email());
    }

    @Test
    void shouldChangeDisplayName() {
        User user = createUser();

        user.changeDisplayName("Jane Doe");

        assertEquals("Jane Doe", user.displayName());
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

    private User createUser() {
        return User.create(
                UserId.generate(),
                TenantId.generate(),
                "john@example.com",
                "John Doe"
        );
    }
}