package com.logistics.identity.infrastructure.persistence.mapper;

import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.persistence.entity.UserSessionEntity;

public final class UserSessionMapper {

    private UserSessionMapper() {}

    public static UserSessionEntity toEntity(UserSession session) {
        if (session == null) {
            throw new IllegalArgumentException("User session cannot be null");
        }

        return new UserSessionEntity(
                session.id(),
                session.userId().value(),
                session.createdAt(),
                session.lastSeenAt(),
                session.expiresAt(),
                session.revokedAt(),
                session.ipAddress(),
                session.userAgent());
    }

    public static UserSession toDomain(UserSessionEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("User session cannot be null");
        }

        return UserSession.reconstitute(
                entity.getId(),
                new UserId(entity.getUserId()),
                entity.getCreatedAt(),
                entity.getLastSeenAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getIpAddress(),
                entity.getUserAgent());
    }
}
