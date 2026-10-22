package com.logistics.identity.infrastructure.persistence.mapper;

import com.logistics.identity.domain.entity.PasswordResetToken;
import com.logistics.identity.infrastructure.persistence.entity.PasswordResetTokenEntity;

public final class PasswordResetTokenMapper {

    private PasswordResetTokenMapper() {}

    public static PasswordResetTokenEntity toEntity(PasswordResetToken token) {
        if (token == null) {
            throw new IllegalArgumentException("Password reset token cannot be null");
        }

        return new PasswordResetTokenEntity(
                token.id(),
                token.userId(),
                token.tokenHash(),
                token.createdAt(),
                token.expiresAt(),
                token.consumedAt(),
                token.revokedAt());
    }

    public static PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Password reset token entity cannot be null");
        }

        return PasswordResetToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getConsumedAt(),
                entity.getRevokedAt());
    }
}
