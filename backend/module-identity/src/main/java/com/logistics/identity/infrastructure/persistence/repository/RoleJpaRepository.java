package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.RoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByTenantIdAndName(UUID tenantId, String name);

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    Optional<RoleEntity> findByTenantIdIsNullAndName(String name);

    boolean existsByTenantIdIsNullAndName(String name);
}
