package com.logistics.identity.domain.repository;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.valueobject.PermissionId;

import java.util.Optional;

public interface PermissionRepository {

    Permission save(Permission permission);

    Optional<Permission> findById(PermissionId permissionId);
    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);
}