package com.logistics.identity.domain.entity;

import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.UserId;
import java.util.Objects;

public final class UserRole {
    private final UserId userId;
    private final RoleId roleId;

    private UserRole(UserId userId, RoleId roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public static UserRole create(UserId userId, RoleId roleId) {
        return new UserRole(
                Objects.requireNonNull(userId, "User ID cannot be null"),
                Objects.requireNonNull(roleId, "Role ID cannot be null"));
    }

    public UserId userId() {
        return userId;
    }

    public RoleId roleId() {
        return roleId;
    }
}
