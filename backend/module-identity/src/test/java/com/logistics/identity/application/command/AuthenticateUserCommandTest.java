package com.logistics.identity.application.command;

import static org.junit.jupiter.api.Assertions.*;

import com.logistics.identity.domain.valueobject.TenantId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthenticateUserCommandTest {

    @Test
    void shouldCreateValidCommand() {
        TenantId tenantId = new TenantId(UUID.randomUUID());

        AuthenticateUserCommand command =
                new AuthenticateUserCommand(tenantId, "  USER@EXAMPLE.COM  ", "SecurePassword123!");

        assertEquals(tenantId, command.tenantId());
        assertEquals("user@example.com", command.email());
        assertEquals("SecurePassword123!", command.password());
    }

    @Test
    void shouldRejectNullTenantId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AuthenticateUserCommand(null, "user@example.com", "password"));
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AuthenticateUserCommand(new TenantId(UUID.randomUUID()), "   ", "password"));
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AuthenticateUserCommand(new TenantId(UUID.randomUUID()), "user@example.com", "   "));
    }
}
