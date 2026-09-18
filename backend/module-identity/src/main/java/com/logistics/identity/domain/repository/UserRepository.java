package com.logistics.identity.domain.repository;

import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByTenantIdAndEmail(
            TenantId tenantId,
            String email
    );

    boolean existsByTenantIdAndEmail(
        TenantId tenantId,
        String email
    );
}