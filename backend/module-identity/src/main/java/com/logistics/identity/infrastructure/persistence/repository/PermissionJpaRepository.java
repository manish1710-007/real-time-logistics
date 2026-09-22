package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.PermissionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, UUID> {

    Optional<PermissionEntity> findByCode(String code);

    boolean existsByCode(String code);
}
