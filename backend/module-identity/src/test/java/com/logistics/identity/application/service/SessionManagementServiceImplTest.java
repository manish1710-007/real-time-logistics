package com.logistics.identity.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.valueobject.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionManagementServiceImplTest {

    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private Clock clock;
    private SessionManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneOffset.UTC);

        service = new SessionManagementServiceImpl(sessionRepository, refreshTokenRepository, clock);
    }

    @Test
    void shouldRevokeSessionAndItsRefreshTokens() {
        Instant now = clock.instant();
        UUID sessionId = UUID.randomUUID();
        UserId userId = UserId.generate();

        UserSession session = UserSession.create(
                sessionId, userId, now.minusSeconds(60), now.plusSeconds(3600), "127.0.0.1", "JUnit");

        RefreshToken token = RefreshToken.create(
                UUID.randomUUID(), sessionId, "token-hash", now.minusSeconds(30), now.plusSeconds(1800));

        when(sessionRepository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));
        when(refreshTokenRepository.findBySessionId(sessionId)).thenReturn(List.of(token));

        service.revokeSession(sessionId, userId);

        assertTrue(session.isRevoked());
        assertEquals(now, session.revokedAt());
        assertTrue(token.isRevoked());
        assertEquals(now, token.revokedAt());

        verify(sessionRepository).save(session);
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void shouldRejectRevokingAnotherUsersSession() {
        UUID sessionId = UUID.randomUUID();
        UserSession session = UserSession.create(
                sessionId,
                UserId.generate(),
                clock.instant().minusSeconds(60),
                clock.instant().plusSeconds(3600),
                "127.0.0.1",
                "JUnit");

        when(sessionRepository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class, () -> service.revokeSession(sessionId, UserId.generate()));

        verify(sessionRepository, never()).save(any());
        verify(refreshTokenRepository, never()).findBySessionId(any());
    }

    @Test
    void shouldRejectUnknownSession() {
        UUID sessionId = UUID.randomUUID();

        when(sessionRepository.findByIdForUpdate(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.revokeSession(sessionId, UserId.generate()));
    }

    @Test
    void shouldRevokeAllActiveSessionsAndTheirTokens() {
        Instant now = clock.instant();
        UserId userId = UserId.generate();

        UserSession first = UserSession.create(
                UUID.randomUUID(), userId, now.minusSeconds(120), now.plusSeconds(3600), "127.0.0.1", "JUnit");

        UserSession second = UserSession.create(
                UUID.randomUUID(), userId, now.minusSeconds(60), now.plusSeconds(7200), "127.0.0.2", "JUnit");

        RefreshToken firstToken = RefreshToken.create(
                UUID.randomUUID(), first.id(), "hash-1", now.minusSeconds(30), now.plusSeconds(1800));

        RefreshToken secondToken = RefreshToken.create(
                UUID.randomUUID(), second.id(), "hash-2", now.minusSeconds(20), now.plusSeconds(3600));

        when(sessionRepository.findActiveByUserId(userId, now)).thenReturn(List.of(first, second));
        when(refreshTokenRepository.findBySessionId(first.id())).thenReturn(List.of(firstToken));
        when(refreshTokenRepository.findBySessionId(second.id())).thenReturn(List.of(secondToken));

        service.revokeAllSessions(userId);

        assertTrue(first.isRevoked());
        assertTrue(second.isRevoked());
        assertTrue(firstToken.isRevoked());
        assertTrue(secondToken.isRevoked());

        verify(sessionRepository).save(first);
        verify(sessionRepository).save(second);
        verify(refreshTokenRepository).save(firstToken);
        verify(refreshTokenRepository).save(secondToken);
    }

    @Test
    void shouldReturnActiveSessionsForUser() {
        Instant now = clock.instant();
        UserId userId = UserId.generate();

        UserSession session = UserSession.create(
                UUID.randomUUID(), userId, now.minusSeconds(60), now.plusSeconds(3600), "127.0.0.1", "JUnit");

        when(sessionRepository.findActiveByUserId(userId, now)).thenReturn(List.of(session));

        assertEquals(List.of(session), service.getActiveSessions(userId));
    }

    @Test
    void shouldRejectNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> service.revokeSession(null, UserId.generate()));

        assertThrows(IllegalArgumentException.class, () -> service.revokeSession(UUID.randomUUID(), null));

        assertThrows(IllegalArgumentException.class, () -> service.revokeAllSessions(null));

        assertThrows(IllegalArgumentException.class, () -> service.getActiveSessions(null));
    }
}
