package com.logistics.identity.domain.entity;

import com.logistics.identity.domain.valueobject.PermissionId;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.RoleType;
import com.logistics.identity.domain.valueobject.TenantId;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Role {

    private final RoleId id;
    private final TenantId tenantId;

    private String name;
    private final RoleType type;

    private final Set<PermissionId> permissionIds;

    private final Instant createdAt;
    private Instant updatedAt;

    private Role(
            RoleId id,
            TenantId tenantId,
            String name,
            RoleType type,
            Set<PermissionId> permissionIds,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Role ID cannot be null");
        this.tenantId = tenantId;
        this.name = requireText(name, "Role name");
        this.type = Objects.requireNonNull(type, "Role type cannot be null");
        this.permissionIds = new HashSet<>(permissionIds);
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
    }

    public static Role createSystemRole(
            RoleId id,
            String name
    ) {
        Instant now = Instant.now();

        return new Role(
                id,
                null,
                name,
                RoleType.SYSTEM,
                Set.of(),
                now,
                now
        );
    }

    public static Role createTenantRole(
            RoleId id,
            TenantId tenantId,
            String name
    ) {
        Instant now = Instant.now();

        return new Role(
                id,
                Objects.requireNonNull(tenantId, "Tenant ID cannot be null"),
                name,
                RoleType.TENANT,
                Set.of(),
                now,
                now
        );
    }

    public static Role reconstitute(
        RoleId id,
        TenantId tenantId,
        String name,
        RoleType type,
        Set<PermissionId> permissionIds,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new Role(
            id,
            tenantId,
            name,
            type,
            permissionIds,
            createdAt,
            updatedAt
        );
    }

    public RoleId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public String name() {
        return name;
    }

    public RoleType type() {
        return type;
    }

    public Set<PermissionId> permissionIds() {
        return Collections.unmodifiableSet(permissionIds);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void rename(String newName) {
        this.name = requireText(newName, "Role name");
        touch();
    }

    public void addPermission(PermissionId permissionId) {
        Objects.requireNonNull(permissionId, "Permission ID cannot be null");

        if (permissionIds.add(permissionId)) {
            touch();
        }
    }

    public void removePermission(PermissionId permissionId) {
        Objects.requireNonNull(permissionId, "Permission ID cannot be null");

        if (permissionIds.remove(permissionId)) {
            touch();
        }
    }

    public boolean hasPermission(PermissionId permissionId) {
        Objects.requireNonNull(permissionId, "Permission ID cannot be null");
        return permissionIds.contains(permissionId);
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