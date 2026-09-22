package com.logistics.identity.domain.repository;

import com.logistics.identity.domain.entity.RefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(UUID refreshTokenId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findBySessionId(UUID sessionId);

    List<RefreshToken> findActiveBySessionId(UUID sessionId, Instant now);

    List<RefreshToken> findExpiredBefore(Instant time);
}
