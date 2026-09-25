package com.logistics.identity.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

class UserRoleTest {

    @Test
    void shouldCreateUserRole() {
        UserId userId = UserId.generate();
        RoleId roleId = RoleId.generate();

        UserRole userRole = UserRole.create(userId, roleId);

        assertEquals(userId, userRole.userId());
        assertEquals(roleId, userRole.roleId());
    }

    @Test
    void shouldRejectNullUserId() {
        RoleId roleId = RoleId.generate();

        assertThrows(NullPointerException.class, () -> UserRole.create(null, roleId));
    }

    @Test
    void shouldRejectNullRoleId() {
        UserId userId = UserId.generate();

        assertThrows(NullPointerException.class, () -> UserRole.create(userId, null));
    }
}

// this remmeber this point! 