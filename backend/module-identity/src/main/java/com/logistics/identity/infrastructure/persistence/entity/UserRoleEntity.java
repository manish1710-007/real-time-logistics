package com.logistics.identity.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
@IdClass(UserRoleEntity.UserRoleId.class)
public class UserRoleEntity {

    @Id
    private UUID userId;

    @Id
    private UUID roleId;

    protected UserRoleEntity() {
        // Required by JPA
    }

    public UserRoleEntity(UUID userId, UUID roleId) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.roleId = Objects.requireNonNull(roleId, "Role ID cannot be null");
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public static class UserRoleId implements Serializable {

        private UUID userId;
        private UUID roleId;

        public UserRoleId() {
            // Required by JPA
        }

        public UserRoleId(UUID userId, UUID roleId) {
            this.userId = userId;
            this.roleId = roleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof UserRoleId that)) {
                return false;
            }

            return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, roleId);
        }
    }
}
