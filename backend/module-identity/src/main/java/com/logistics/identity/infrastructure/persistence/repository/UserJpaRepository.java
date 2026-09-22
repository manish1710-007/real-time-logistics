package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}
