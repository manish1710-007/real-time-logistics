package com.logistics.identity.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RefreshTokenGeneratorTest {

    private RefreshTokenGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RefreshTokenGenerator();
    }

    @Test
    void generateTokenShouldReturnUniqueUrlSafeTokens() {
        String first = generator.generateToken();
        String second = generator.generateToken();

        assertNotEquals(first, second);
        assertTrue(first.matches("[A-Za-z0-9_-]+"));
        assertTrue(second.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void generateTokenShouldProduceExpectedLength() {
        String token = generator.generateToken();

        // 32 random bytes encoded as unpadded Base64 URL-safe text.
        assertEquals(43, token.length());
    }

    @Test
    void hashTokenShouldBeDeterministic() {
        String rawToken = generator.generateToken();

        assertEquals(generator.hashToken(rawToken), generator.hashToken(rawToken));
    }

    @Test
    void differentTokensShouldProduceDifferentHashes() {
        String first = generator.generateToken();
        String second = generator.generateToken();

        assertNotEquals(generator.hashToken(first), generator.hashToken(second));
    }

    @Test
    void hashTokenShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> generator.hashToken(null));
    }

    @Test
    void hashTokenShouldRejectBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> generator.hashToken("   "));
    }
}
