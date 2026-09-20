package com.logistics.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionEntity.RolePermissionId.class)
public class RolePermissionEntity {

    @Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Id
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    protected RolePermissionEntity() {
    }

    public RolePermissionEntity(UUID roleId, UUID permissionId) {
        this.roleId = Objects.requireNonNull(
                roleId,
                "Role ID cannot be null"
        );

        this.permissionId = Objects.requireNonNull(
                permissionId,
                "Permission ID cannot be null"
        );
    }

    public UUID getRoleId() {
        return roleId;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public static class RolePermissionId implements Serializable {

        private UUID roleId;
        private UUID permissionId;

        public RolePermissionId() {
        }

        public RolePermissionId(UUID roleId, UUID permissionId) {
            this.roleId = roleId;
            this.permissionId = permissionId;
        }

        public UUID getRoleId() {
            return roleId;
        }

        public UUID getPermissionId() {
            return permissionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof RolePermissionId that)) {
                return false;
            }

            return Objects.equals(roleId, that.roleId)
                    && Objects.equals(permissionId, that.permissionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, permissionId);
        }
    }
}