package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.RolePermissionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionJpaRepository
        extends JpaRepository<RolePermissionEntity, RolePermissionEntity.RolePermissionId> {

    List<RolePermissionEntity> findByRoleId(UUID roleId);

    void deleteByRoleId(UUID roleId);
}
