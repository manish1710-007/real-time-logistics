package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import com.logistics.identity.infrastructure.persistence.mapper.RefreshTokenMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token cannot be null");
        }

        RefreshTokenEntity entity = RefreshTokenMapper.toEntity(refreshToken);

        RefreshTokenEntity savedEntity = refreshTokenJpaRepository.save(entity);

        return RefreshTokenMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findById(UUID refreshTokenId) {
        if (refreshTokenId == null) {
            throw new IllegalArgumentException("Refresh token ID cannot be null");
        }

        return refreshTokenJpaRepository.findById(refreshTokenId).map(RefreshTokenMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        validateTokenHash(tokenHash);

        return refreshTokenJpaRepository.findByTokenHash(tokenHash).map(RefreshTokenMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findBySessionId(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }

        return refreshTokenJpaRepository.findBySessionId(sessionId).stream()
                .map(RefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findActiveBySessionId(UUID sessionId, Instant now) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }

        if (now == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }

        return refreshTokenJpaRepository.findBySessionIdAndRevokedAtIsNull(sessionId).stream()
                .map(RefreshTokenMapper::toDomain)
                .filter(token -> token.isActive(now))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findExpiredBefore(Instant time) {
        if (time == null) {
            throw new IllegalArgumentException("Expiration time cannot be null");
        }

        return refreshTokenJpaRepository.findByExpiresAtBefore(time).stream()
                .map(RefreshTokenMapper::toDomain)
                .toList();
    }

    private static void validateTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash cannot be null or blank");
        }
    }
}
