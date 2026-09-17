package com.logistics.identity.domain.entity;

import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.TenantStatus;

import java.time.Instant;
import java.util.Objects;

public final class Tenant {

    private final TenantId id;
    private String name;
    private String slug;
    private TenantStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Tenant(
            TenantId id,
            String name,
            String slug,
            TenantStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Tenant ID cannot be null");
        this.name = requireText(name, "Tenant name");
        this.slug = requireText(slug, "Tenant slug");
        this.status = Objects.requireNonNull(status, "Tenant status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
    }

    public static Tenant create(
            TenantId id,
            String name,
            String slug
    ) {
        Instant now = Instant.now();

        return new Tenant(
                id,
                name,
                slug,
                TenantStatus.ACTIVE,
                now,
                now
        );
    }

    public static Tenant reconstitute(
        TenantId id,
        String name,
        String slug,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    return new Tenant(
            id,
            name,
            slug,
            status,
            createdAt,
            updatedAt
    );
}

    public TenantId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String slug() {
        return slug;
    }

    public TenantStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void rename(String newName) {
        this.name = requireText(newName, "Tenant name");
        touch();
    }

    public void changeSlug(String newSlug) {
        this.slug = requireText(newSlug, "Tenant slug");
        touch();
    }

    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
        touch();
    }

    public void activate() {
        this.status = TenantStatus.ACTIVE;
        touch();
    }

    public void disable() {
        this.status = TenantStatus.DISABLED;
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