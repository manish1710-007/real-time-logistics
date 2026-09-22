package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.TenantEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantJpaRepository extends JpaRepository<TenantEntity, UUID> {

    Optional<TenantEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
