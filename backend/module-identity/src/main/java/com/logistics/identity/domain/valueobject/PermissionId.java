package com.logistics.identity.domain.valueobject;

import java.util.UUID;

public record PermissionId(UUID value) {

    public PermissionId {
        if (value == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }
    }
    
    public static PermissionId generate() {
        return new PermissionId(UUID.randomUUID());
    }
}