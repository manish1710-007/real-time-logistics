package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.domain.entity.Tenant;
import com.logistics.identity.domain.repository.TenantRepository;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.infrastructure.persistence.entity.TenantEntity;
import com.logistics.identity.infrastructure.persistence.mapper.TenantMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantJpaRepository tenantJpaRepository;

    public TenantRepositoryImpl(TenantJpaRepository tenantJpaRepository) {
        this.tenantJpaRepository = tenantJpaRepository;
    }

    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity entity = TenantMapper.toEntity(tenant);

        TenantEntity savedEntity = tenantJpaRepository.save(entity);

        return TenantMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Tenant> findById(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }

        return tenantJpaRepository.findById(tenantId.value()).map(TenantMapper::toDomain);
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Tenant slug cannot be null or blank");
        }

        return tenantJpaRepository.findBySlug(slug).map(TenantMapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Tenant slug cannot be null or blank");
        }

        return tenantJpaRepository.existsBySlug(slug);
    }
}
