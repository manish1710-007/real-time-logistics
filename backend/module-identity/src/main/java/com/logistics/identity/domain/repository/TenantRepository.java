package com.logistics.identity.domain.repository;

import com.logistics.identity.domain.entity.Tenant;
import com.logistics.identity.domain.valueobject.TenantId;

import java.util.Optional;

public interface TenantRepository {

    Tenant save(Tenant tenant);

    Optional<Tenant> findById(TenantId tenantId);

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}