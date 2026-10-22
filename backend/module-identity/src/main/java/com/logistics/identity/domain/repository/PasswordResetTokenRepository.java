package com.logistics.identity.domain.repository;

import com.logistics.identity.domain.entity.PasswordResetToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHashForUpdate(String tokenHash);

    List<PasswordResetToken> findActiveByUserId(UUID userId, Instant now);

    void revokeActiveByUserId(UUID userId, Instant now);
}
