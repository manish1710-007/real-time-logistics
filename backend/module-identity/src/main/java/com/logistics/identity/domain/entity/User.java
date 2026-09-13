package com.logistics.identity.domain.entity;

import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.domain.valueobject.UserStatus;

import java.time.Instant;
import java.util.Objects;

public final class User {

    private final UserId id;
    private final TenantId tenantId;

    private String email;
    private String displayName;
    private UserStatus status;

    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            UserId id,
            TenantId tenantId,
            String email,
            String displayName,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.email = requireText(email, "User email");
        this.displayName = requireText(displayName, "User display name");
        this.status = Objects.requireNonNull(status, "User status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
    }

    public static User create(
            UserId id,
            TenantId tenantId,
            String email,
            String displayName
    ) {
        Instant now = Instant.now();

        return new User(
                id,
                tenantId,
                email,
                displayName,
                UserStatus.INVITED,
                now,
                now
        );
    }

    public UserId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public String email() {
        return email;
    }

    public String displayName() {
        return displayName;
    }

    public UserStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void changeEmail(String newEmail) {
        this.email = requireText(newEmail, "User email");
        touch();
    }

    public void changeDisplayName(String newDisplayName) {
        this.displayName = requireText(newDisplayName, "User display name");
        touch();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        touch();
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        touch();
    }

    public void lock() {
        this.status = UserStatus.LOCKED;
        touch();
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
        touch();
    }

    public void markDeleted() {
        this.status = UserStatus.DELETED;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }

        return value;
    }
}