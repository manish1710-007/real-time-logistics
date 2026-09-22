package com.logistics.identity.domain.valueobject;

import java.util.UUID;

public record RoleId(UUID value) {

    public RoleId {
        if (value == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
    }

    public static RoleId generate() {
        return new RoleId(UUID.randomUUID());
    }
}
