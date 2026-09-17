package com.logistics.identity.infrastructure.persistence.mapper;

import com.logistics.identity.domain.entity.Tenant;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.TenantStatus;
import com.logistics.identity.infrastructure.persistence.entity.TenantEntity;

public final class TenantMapper {

    private TenantMapper() {
        // Utility class
    }

    public static TenantEntity toEntity(Tenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant cannot be null");
        }

        return new TenantEntity(
                tenant.id().value(),
                tenant.name(),
                tenant.slug(),
                tenant.status().name(),
                tenant.createdAt(),
                tenant.updatedAt()
        );
    }

    public static Tenant toDomain(TenantEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Tenant entity cannot be null");
        }

        return Tenant.reconstitute(
                new TenantId(entity.getId()),
                entity.getName(),
                entity.getSlug(),
                TenantStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
} 