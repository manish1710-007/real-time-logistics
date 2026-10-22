package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.PasswordResetTokenEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            SELECT token
            FROM PasswordResetTokenEntity token
            WHERE token.tokenHash = :tokenHash
            """)
    Optional<PasswordResetTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Query(
            """
            SELECT token
            FROM PasswordResetTokenEntity token
            WHERE token.userId = :userId
              AND token.consumedAt IS NULL
              AND token.revokedAt IS NULL
              AND token.expiresAt > :now
            """)
    List<PasswordResetTokenEntity> findActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Query(
            """
            UPDATE PasswordResetTokenEntity token
            SET token.revokedAt = :now
            WHERE token.userId = :userId
              AND token.consumedAt IS NULL
              AND token.revokedAt IS NULL
              AND token.expiresAt > :now
            """)
    int revokeActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
