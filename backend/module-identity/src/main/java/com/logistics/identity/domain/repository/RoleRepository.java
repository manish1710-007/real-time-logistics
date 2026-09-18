package com.logistics.identity.domain.repository;

import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.TenantId;

import java.util.Optional;

public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(RoleId roleId);

    Optional<Role> findByTenantIdAndName(
            TenantId tenantId,
            String name
    );

    boolean existsByTenantIdAndName(
            TenantId tenantId,
            String name
    );
}