package com.logistics.identity.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenGenerator {

    private static final int TOKEN_BYTES = 32; // 256 bits

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String hashToken(String rawToken) {
        Objects.requireNonNull(rawToken, "Raw refresh token cannot be null");

        if (rawToken.isBlank()) {
            throw new IllegalArgumentException("Raw refresh token cannot be blank");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalArgumentException("SHA-256 is unavailable", exception);
        }
    }
}
