package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.persistence.entity.UserEntity;
import com.logistics.identity.infrastructure.persistence.mapper.UserMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        UserEntity entity = UserMapper.toEntity(user);

        UserEntity savedEntity = userJpaRepository.save(entity);

        return UserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return userJpaRepository.findById(userId.value()).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByTenantIdAndEmail(TenantId tenantId, String email) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("User email cannot be null or blank");
        }

        return userJpaRepository.findByTenantIdAndEmail(tenantId.value(), email).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByTenantIdAndEmail(TenantId tenantId, String email) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("User email cannot be null or blank");
        }

        return userJpaRepository.existsByTenantIdAndEmail(tenantId.value(), email);
    }
}
