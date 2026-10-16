package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.valueobject.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionManagementServiceImpl implements SessionManagementService {

    private final UserSessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public SessionManagementServiceImpl(
            UserSessionRepository sessionRepository, RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void revokeSession(UUID sessionId, UserId userId) {
        if (sessionId == null || userId == null) {
            throw new IllegalArgumentException("Session ID and user ID are required");
        }

        UserSession session = sessionRepository
                .findByIdForUpdate(sessionId)
                .filter(s -> s.userId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("SEssion not found"));

        revoke(session, clock.instant());
    }

    @Override
    @Transactional
    public void revokeAllSessions(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Instant now = clock.instant();

        List<UserSession> sessions = sessionRepository.findActiveByUserId(userId, now);

        for (UserSession session : sessions) {
            revoke(session, now);
        }
    }

    @Override
    @Transactional
    public List<UserSession> getActiveSessions(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User Id is required");
        }

        return sessionRepository.findActiveByUserId(userId, clock.instant());
    }

    private void revoke(UserSession session, Instant now) {
        session.revoke(now);
        sessionRepository.save(session);

        List<RefreshToken> tokens = refreshTokenRepository.findBySessionId(session.id());

        for (RefreshToken token : tokens) {
            token.revoke(now);
            refreshTokenRepository.save(token);
        }
    }
}
