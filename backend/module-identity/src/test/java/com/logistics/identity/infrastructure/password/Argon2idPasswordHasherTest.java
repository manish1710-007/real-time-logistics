package com.logistics.identity.infrastructure.password;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Argon2idPasswordHasherTest {

    private Argon2idPasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new Argon2idPasswordHasher();
    }

    @Test
    void shouldHashPassword() {
        String password = "SecurePassword123!";

        String hash = passwordHasher.hash(password);

        assertNotNull(hash);
        assertFalse(hash.isBlank());
        assertTrue(hash.startsWith("$argon2id$"));
        assertNotEquals(password, hash);
    }

    @Test
    void shouldMatchCorrectPassword() {
        String password = "SecurePassword123!";
        String hash = passwordHasher.hash(password);

        assertTrue(passwordHasher.matches(password, hash));
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String password = "SecurePassword123!";
        String wrongPassword = "WrongPassword123!";
        String hash = passwordHasher.hash(password);

        assertFalse(passwordHasher.matches(wrongPassword, hash));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String password = "SecurePassword123!";

        String firstHash = passwordHasher.hash(password);
        String secondHash = passwordHasher.hash(password);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void shouldRejectNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> passwordHasher.hash(null));
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> passwordHasher.hash("   "));
    }

    @Test
    void shouldRejectNullPasswordHash() {
        assertThrows(IllegalArgumentException.class, () -> passwordHasher.matches("password", null));
    }

    @Test
    void shouldRejectBlankPasswordHash() {
        assertThrows(IllegalArgumentException.class, () -> passwordHasher.matches("password", "   "));
    }
}
