package com.logistics.identity.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

    @Test
    void shouldAcceptValidPassword() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("SecurePassword123!"));
    }

    @Test
    void shouldRejectNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.validate(null));
    }

    @Test
    void shouldRejectPasswordShorterThanMinimumLength() {
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.validate("1234567"));
    }

    @Test
    void shouldAcceptPasswordAtMinimumLength() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("12345678"));
    }

    @Test
    void shouldAcceptPasswordAtMaximumLength() {
        String password = "a".repeat(128);

        assertDoesNotThrow(() -> PasswordPolicy.validate(password));
    }

    @Test
    void shouldRejectPasswordLongerThanMaximumLength() {
        String password = "a".repeat(129);

        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.validate(password));
    }
}
