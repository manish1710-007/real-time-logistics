package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT token FROM RefreshTokenEntity token WHERE token.tokenHash = :tokenHash")
    Optional<RefreshTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    Optional<RefreshTokenEntity> findByTokenHash(@Param("tokenHash") String tokenHash);

    List<RefreshTokenEntity> findBySessionId(UUID sessionId);

    List<RefreshTokenEntity> findBySessionIdAndRevokedAtIsNull(UUID sessionId);

    List<RefreshTokenEntity> findByExpiresAtBefore(Instant time);
}
