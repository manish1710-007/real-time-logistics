package com.logistics.identity.domain.entity;

import java.time.Instant;
import java.util.Objects;

import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.domain.valueobject.UserStatus;

public final class User {

    private final UserId id;
    private final TenantId tenantId;

    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus status;
    private boolean emailVerified;
    private Instant lastLoginAt;

    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private User(
            UserId id,
            TenantId tenantId,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String phone,
            UserStatus status,
            boolean emailVerified,
            Instant lastLoginAt,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.email = requireText(email, "User email");
        this.passwordHash = requireText(passwordHash, "Password hash");
        this.firstName = requireText(firstName, "First name");
        this.lastName = requireText(lastName, "Last name");
        this.phone = normalizeOptionalText(phone);
        this.status = Objects.requireNonNull(status, "User status cannot be null");
        this.emailVerified = emailVerified;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");

        if (version < 0) {
            throw new IllegalArgumentException("Version cannot be negative");
        }

        this.version = version;
    }

    public static User create(
            UserId id,
            TenantId tenantId,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String phone
    ) {
        Instant now = Instant.now();

        return new User(
                id,
                tenantId,
                email,
                passwordHash,
                firstName,
                lastName,
                phone,
                UserStatus.INVITED,
                false,
                null,
                now,
                now,
                0
        );
    }

    public static User reconstitute(
            UserId id,
            TenantId tenantId,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String phone,
            UserStatus status,
            boolean emailVerified,
            Instant lastLoginAt,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new User(
                id,
                tenantId,
                email,
                passwordHash,
                firstName,
                lastName,
                phone,
                status,
                emailVerified,
                lastLoginAt,
                createdAt,
                updatedAt,
                version
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

    public String passwordHash() {
        return passwordHash;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String phone() {
        return phone;
    }

    public UserStatus status() {
        return status;
    }

    public boolean emailVerified() {
        return emailVerified;
    }

    public Instant lastLoginAt() {
        return lastLoginAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public long version() {
        return version;
    }

    public void changeEmail(String newEmail) {
        this.email = requireText(newEmail, "User email");
        touch();
    }

    public void changeName(String newFirstName, String newLastName) {
        this.firstName = requireText(newFirstName, "First name");
        this.lastName = requireText(newLastName, "Last name");
        touch();
    }

    public void changePhone(String newPhone) {
        this.phone = normalizeOptionalText(newPhone);
        touch();
    }

    public void updatePasswordHash(String newPasswordHash) {
        this.passwordHash = requireText(newPasswordHash, "Password hash");
        touch();
    }

    public void verifyEmail() {
        this.emailVerified = true;
        touch();
    }

    public void recordLogin(Instant loginAt) {
        this.lastLoginAt = Objects.requireNonNull(
                loginAt,
                "Login time cannot be null"
        );
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

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}