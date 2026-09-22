package com.logistics.identity.infrastructure.persistence.mapper;

import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.domain.valueobject.UserStatus;
import com.logistics.identity.infrastructure.persistence.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
        // Utility class
    }

    public static UserEntity toEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return new UserEntity(
                user.id().value(),
                user.tenantId().value(),
                user.email(),
                user.passwordHash(),
                user.firstName(),
                user.lastName(),
                user.phone(),
                user.status().name(),
                user.emailVerified(),
                user.lastLoginAt(),
                user.createdAt(),
                user.updatedAt(),
                user.version());
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("User entity cannot be null");
        }

        return User.reconstitute(
                new UserId(entity.getId()),
                new TenantId(entity.getTenantId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                UserStatus.valueOf(entity.getStatus()),
                entity.isEmailVerified(),
                entity.getLastLoginAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }
}
