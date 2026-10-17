package com.logistics.identity.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.logistics.identity.domain.entity.PasswordResetToken;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHashForUpdate(String tokenHash);

    List<PasswordResetToken> findActiveByUserId(UUID userId, Instant now);

    void revokeActiveByUserId(UUID userId, Instant now);
}