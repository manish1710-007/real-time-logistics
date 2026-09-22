package com.logistics.identity.infrastructure.persistence.mapper;

import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.valueobject.PermissionId;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.RoleType;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.infrastructure.persistence.entity.RoleEntity;
import java.util.Set;

public final class RoleMapper {

    private RoleMapper() {
        // Utility class
    }

    public static RoleEntity toEntity(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        return new RoleEntity(
                role.id().value(),
                role.tenantId() != null ? role.tenantId().value() : null,
                role.name(),
                role.type().name(),
                role.createdAt(),
                role.updatedAt());
    }

    public static Role toDomain(RoleEntity entity, Set<PermissionId> permissionIds) {
        if (entity == null) {
            throw new IllegalArgumentException("Role entity cannot be null");
        }

        if (permissionIds == null) {
            throw new IllegalArgumentException("Permission IDs cannot be null");
        }

        TenantId tenantId = entity.getTenantId() != null ? new TenantId(entity.getTenantId()) : null;

        return Role.reconstitute(
                new RoleId(entity.getId()),
                tenantId,
                entity.getName(),
                RoleType.valueOf(entity.getType()),
                permissionIds,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
