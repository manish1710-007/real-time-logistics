package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.domain.entity.PasswordResetToken;
import com.logistics.identity.domain.repository.PasswordResetTokenRepository;
import com.logistics.identity.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.logistics.identity.infrastructure.persistence.mapper.PasswordResetTokenMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryImpl(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        if (token == null) {
            throw new IllegalArgumentException("Password reset token cannot be null");
        }

        PasswordResetTokenEntity entity = PasswordResetTokenMapper.toEntity(token);

        PasswordResetTokenEntity savedEntity = jpaRepository.save(entity);

        return PasswordResetTokenMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional
    public Optional<PasswordResetToken> findByTokenHashForUpdate(String tokenHash) {

        validateTokenHash(tokenHash);

        return jpaRepository.findByTokenHashForUpdate(tokenHash).map(PasswordResetTokenMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PasswordResetToken> findActiveByUserId(UUID userId, Instant now) {

        validateUserAndTime(userId, now);

        return jpaRepository.findActiveByUserId(userId, now).stream()
                .map(PasswordResetTokenMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void revokeActiveByUserId(UUID userId, Instant now) {
        validateUserAndTime(userId, now);

        jpaRepository.revokeActiveByUserId(userId, now);
    }

    private static void validateTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash cannot be null or blank");
        }
    }

    private static void validateUserAndTime(UUID userId, Instant now) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (now == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }
    }
}
