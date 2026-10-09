package com.logistics.identity.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.identity.application.command.RefreshTokenCommand;
import com.logistics.identity.application.dto.RefreshTokenResult;
import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.security.RefreshTokenGenerator;
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
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private RoleLoader roleLoader;

    @Mock
    private PermissionLoader permissionLoader;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    private Clock clock;
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneOffset.UTC);

        refreshTokenService = new RefreshTokenServiceImpl(
                refreshTokenRepository,
                userSessionRepository,
                userRepository,
                refreshTokenGenerator,
                roleLoader,
                permissionLoader,
                accessTokenIssuer,
                clock);
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        String rawToken = "unknown-refresh-token";
        String tokenHash = "hashed-refresh-token";

        when(refreshTokenGenerator.hashToken(rawToken)).thenReturn(tokenHash);
        when(refreshTokenRepository.findByTokenHashForUpdate(tokenHash)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.refreshToken(new RefreshTokenCommand(rawToken)));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void shouldRevokeSessionWhenReplacedRefreshTokenIsReused() {
        Instant now = clock.instant();

        UUID sessionId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        UUID replacementTokenId = UUID.randomUUID();

        UserSession session = UserSession.create(
                sessionId, UserId.generate(), now.minusSeconds(60), now.plusSeconds(3600), "127.0.0.1", "JUnit");

        RefreshToken reusedToken = RefreshToken.reconstitute(
                tokenId,
                sessionId,
                "stored-token-hash",
                now.minusSeconds(60),
                now.plusSeconds(1800),
                now.minusSeconds(30),
                replacementTokenId);

        when(refreshTokenGenerator.hashToken("reused-raw-token")).thenReturn("stored-token-hash");
        when(refreshTokenRepository.findByTokenHashForUpdate("stored-token-hash"))
                .thenReturn(Optional.of(reusedToken));
        when(userSessionRepository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));

        RefreshTokenReuseDetectedException exception = assertThrows(
                RefreshTokenReuseDetectedException.class,
                () -> refreshTokenService.refreshToken(new RefreshTokenCommand("reused-raw-token")));

        assertEquals("Refresh token reuse detected", exception.getMessage());
        assertTrue(session.isRevoked());
        assertEquals(now, session.revokedAt());

        verify(userSessionRepository).save(session);
    }

    @Test
    void shouldRotateValidRefreshTokenAndIssueNewTokenPair() {
        Instant now = clock.instant();

        UUID sessionId = UUID.randomUUID();
        UUID oldTokenId = UUID.randomUUID();

        UserId userId = UserId.generate();
        TenantId tenantId = new TenantId(UUID.randomUUID());

        UserSession session = UserSession.create(
                sessionId, userId, now.minusSeconds(60), now.plusSeconds(7200), "127.0.0.1", "JUnit");

        RefreshToken oldToken =
                RefreshToken.create(oldTokenId, sessionId, "old-hash", now.minusSeconds(60), now.plusSeconds(3600));

        User user = mock(User.class);
        when(user.id()).thenReturn(userId);
        when(user.tenantId()).thenReturn(tenantId);

        List<Role> roles = List.of(mock(Role.class));
        List<Permission> permissions = List.of(mock(Permission.class));

        when(refreshTokenGenerator.hashToken("old-raw-token")).thenReturn("old-hash");
        when(refreshTokenRepository.findByTokenHashForUpdate("old-hash")).thenReturn(Optional.of(oldToken));
        when(userSessionRepository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleLoader.loadRole(userId)).thenReturn(roles);
        when(permissionLoader.loadPermissions(roles)).thenReturn(permissions);

        when(refreshTokenGenerator.generateToken()).thenReturn("new-raw-token");
        when(refreshTokenGenerator.hashToken("new-raw-token")).thenReturn("new-hash");

        when(accessTokenIssuer.issue(userId, tenantId, roles, permissions))
                .thenReturn(new IssuedAccessToken("new-access-token", now.plusSeconds(900)));

        RefreshTokenResult result = refreshTokenService.refreshToken(new RefreshTokenCommand("old-raw-token"));

        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-raw-token", result.refreshToken());
        assertEquals(now.plusSeconds(900), result.accessTokenExpiresAt());
        assertEquals(oldToken.expiresAt(), result.refreshTokenExpiresAt());

        assertTrue(oldToken.isRevoked());
        assertTrue(oldToken.isReplaced());

        verify(refreshTokenRepository).save(oldToken);
        verify(refreshTokenRepository)
                .save(argThat(token -> token.sessionId().equals(sessionId)
                        && token.tokenHash().equals("new-hash")
                        && token.expiresAt().equals(oldToken.expiresAt())
                        && token.isActive(now)));
    }

    @Test
    void shouldRejectRefreshWhenSessionIsRevoked() {
        Instant now = clock.instant();

        UUID sessionId = UUID.randomUUID();

        UserSession session = UserSession.reconstitute(
                sessionId,
                UserId.generate(),
                now.minusSeconds(120),
                now.minusSeconds(60),
                now.plusSeconds(3600),
                now.minusSeconds(30),
                "127.0.0.1",
                "JUnit");

        RefreshToken refreshToken = RefreshToken.create(
                UUID.randomUUID(), sessionId, "stored-token-hash", now.minusSeconds(60), now.plusSeconds(1800));

        when(refreshTokenGenerator.hashToken("valid-raw-token")).thenReturn("stored-token-hash");

        when(refreshTokenRepository.findByTokenHashForUpdate("stored-token-hash"))
                .thenReturn(Optional.of(refreshToken));

        when(userSessionRepository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.refreshToken(new RefreshTokenCommand("valid-raw-token")));

        assertEquals("Refresh token session is inactive", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(accessTokenIssuer, never()).issue(any(), any(), anyList(), anyList());
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        Instant now = clock.instant();

        UUID sessionId = UUID.randomUUID();

        UserSession session = UserSession.create(
                sessionId, UserId.generate(), now.minusSeconds(120), now.plusSeconds(3600), "127.0.0.1", "JUnit");

        RefreshToken expiredToken =
                RefreshToken.create(UUID.randomUUID(), sessionId, "expired-token-hash", now.minusSeconds(3600), now);

        when(refreshTokenGenerator.hashToken("expired-raw-token")).thenReturn("expired-token-hash");

        when(refreshTokenRepository.findByTokenHashForUpdate("expired-token-hash"))
                .thenReturn(Optional.of(expiredToken));

        when(userSessionRepository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.refreshToken(new RefreshTokenCommand("expired-raw-token")));

        assertEquals("Refresh token has expired", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(accessTokenIssuer, never()).issue(any(), any(), anyList(), anyList());
    }
}
