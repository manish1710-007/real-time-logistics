package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.UserSessionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, UUID> {

    List<UserSessionEntity> findByUserId(UUID userId);

    List<UserSessionEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

    List<UserSessionEntity> findByExpiresAtBefore(java.time.Instant time);
}
