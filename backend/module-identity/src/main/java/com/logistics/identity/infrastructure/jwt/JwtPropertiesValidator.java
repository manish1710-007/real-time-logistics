package com.logistics.identity.infrastructure.jwt;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class JwtPropertiesValidator {

    private static final int MIN_SECRET_LENGTH = 32;

    public JwtPropertiesValidator(JwtProperties properties) {
        validate(properties);
    }

    private void validate(JwtProperties properties) {
        requireText(properties.getIssuer(), "JWT issuer");
        requireText(properties.getAudience(), "JWT audience");

        String secret = properties.getSecret();

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must be configured");
        }

        int secretBytes = secret.getBytes(StandardCharsets.UTF_8).length;

        if (secretBytes < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException("JWT signing secret must contain at least 32 bytes");
        }

        if (properties.getAccessTokenTtl() == null
                || properties.getAccessTokenTtl().isZero()
                || properties.getAccessTokenTtl().isNegative()) {
            throw new IllegalArgumentException("JWT access-token TTL must be +ve");
        }
    }

    private void requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be configured");
        }
    }
}
