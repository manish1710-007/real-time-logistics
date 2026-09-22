package com.logistics.identity.infrastructure.persistence.mapper;

import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.infrastructure.persistence.entity.RefreshTokenEntity;

public final class RefreshTokenMapper {

    private RefreshTokenMapper() {}

    public static RefreshTokenEntity toEntity(RefreshToken token) {
        if (token == null) {
            throw new IllegalArgumentException("Refresh token cannot be null");
        }

        return new RefreshTokenEntity(
                token.id(),
                token.sessionId(),
                token.tokenHash(),
                token.createdAt(),
                token.expiresAt(),
                token.revokedAt(),
                token.replacedByTokenId());
    }

    public static RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Refresh token entity cannot be null");
        }

        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getSessionId(),
                entity.getTokenHash(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getReplacedByTokenId());
    }
}
