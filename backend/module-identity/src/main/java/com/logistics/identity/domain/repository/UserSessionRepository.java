package com.logistics.identity.domain.repository;

import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.valueobject.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository {

    UserSession save(UserSession session);

    Optional<UserSession> findById(UUID sessionId);

    List<UserSession> findByUserId(UserId userId);

    List<UserSession> findActiveByUserId(UserId userId, Instant now);

    List<UserSession> findExpiredBefore(Instant time);
}
