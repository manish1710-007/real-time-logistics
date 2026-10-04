package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.UserSessionEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT session FROM UserSessionEntity session WHERE session.id = :sessionId")
    Optional<UserSessionEntity> findByIdForUpdate(@Param("sessionId") UUID sessionId);

    List<UserSessionEntity> findByUserId(UUID userId);

    List<UserSessionEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

    List<UserSessionEntity> findByExpiresAtBefore(java.time.Instant time);
}
