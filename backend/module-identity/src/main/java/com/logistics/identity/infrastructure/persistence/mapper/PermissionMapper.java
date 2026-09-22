package com.logistics.identity.infrastructure.persistence.mapper;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.valueobject.PermissionId;
import com.logistics.identity.infrastructure.persistence.entity.PermissionEntity;

public final class PermissionMapper {

    private PermissionMapper() {}

    public static PermissionEntity toEntity(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        return new PermissionEntity(permission.id().value(), permission.code(), permission.description());
    }

    public static Permission toDomain(PermissionEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Permission entity cannot be null");
        }

        return Permission.reconstitute(new PermissionId(entity.getId()), entity.getCode(), entity.getDescription());
    }
}
