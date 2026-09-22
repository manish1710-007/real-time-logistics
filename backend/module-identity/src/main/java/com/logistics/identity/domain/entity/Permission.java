package com.logistics.identity.domain.entity;

import com.logistics.identity.domain.valueobject.PermissionId;
import java.util.Objects;

public final class Permission {

    private final PermissionId id;
    private final String code;
    private final String description;

    private Permission(PermissionId id, String code, String description) {
        this.id = Objects.requireNonNull(id, "Permission ID cannot be null");
        this.code = requireText(code, "Permission code");
        this.description = requireText(description, "Permission description");
    }

    public static Permission create(PermissionId id, String code, String description) {
        return new Permission(id, code, description);
    }

    public static Permission reconstitute(PermissionId id, String code, String description) {
        return new Permission(id, code, description);
    }

    public PermissionId id() {
        return id;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }

        return value;
    }
}
