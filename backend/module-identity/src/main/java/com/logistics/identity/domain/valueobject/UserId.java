package com.logistics.identity.domain.valueobject;

import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        if (value == null ) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
}