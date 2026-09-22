package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.persistence.entity.UserSessionEntity;
import com.logistics.identity.infrastructure.persistence.mapper.UserSessionMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserSessionRepositoryImpl implements UserSessionRepository {

    private final UserSessionJpaRepository userSessionJpaRepository;

    public UserSessionRepositoryImpl(UserSessionJpaRepository userSessionJpaRepository) {
        this.userSessionJpaRepository = userSessionJpaRepository;
    }

    @Override
    @Transactional
    public UserSession save(UserSession session) {
        if (session == null) {
            throw new IllegalArgumentException("User session cannot be null");
        }

        UserSessionEntity entity = UserSessionMapper.toEntity(session);

        UserSessionEntity savedEntity = userSessionJpaRepository.save(entity);

        return UserSessionMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSession> findById(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }

        return userSessionJpaRepository.findById(sessionId).map(UserSessionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSession> findByUserId(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return userSessionJpaRepository.findByUserId(userId.value()).stream()
                .map(UserSessionMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSession> findActiveByUserId(UserId userId, Instant now) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (now == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }

        return userSessionJpaRepository.findByUserIdAndRevokedAtIsNull(userId.value()).stream()
                .map(UserSessionMapper::toDomain)
                .filter(session -> session.isActive(now))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSession> findExpiredBefore(Instant time) {
        if (time == null) {
            throw new IllegalArgumentException("Expiration time cannot be null");
        }

        return userSessionJpaRepository.findByExpiresAtBefore(time).stream()
                .map(UserSessionMapper::toDomain)
                .toList();
    }
}
