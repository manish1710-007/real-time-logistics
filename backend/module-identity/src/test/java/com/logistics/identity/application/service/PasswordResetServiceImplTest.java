package com.logistics.identity.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.logistics.identity.domain.entity.PasswordResetToken;
import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.repository.PasswordResetTokenRepository;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.service.PasswordHasher;
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
class PasswordResetServiceImplTest {

    @Mock private PasswordResetTokenRepository resetTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserSessionRepository sessionRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private PasswordResetEmailSender emailSender;

    private RefreshTokenGenerator tokenGenerator;
    private Clock clock;
    private PasswordResetServiceImpl service;

    private final Instant now = Instant.parse("2026-10-20T12:00:00Z");

    @BeforeEach
    void setUp() {
        tokenGenerator = new RefreshTokenGenerator();
        clock = Clock.fixed(now, ZoneOffset.UTC);

        service = new PasswordResetServiceImpl(
                resetTokenRepository,
                userRepository,
                sessionRepository,
                refreshTokenRepository,
                tokenGenerator,
                passwordHasher,
                emailSender,
                clock);
    }

    @Test
    void shouldResetPasswordConsumeTokenAndRevokeSessions() {
        String rawToken = tokenGenerator.generateToken();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        UUID userUuid = UUID.randomUUID();
        UserId userId = new UserId(userUuid);
        TenantId tenantId = TenantId.generate();

        PasswordResetToken resetToken = PasswordResetToken.create(
                UUID.randomUUID(),
                userUuid,
                tokenHash,
                now.minusSeconds(60),
                now.plusSeconds(900));

        User user = User.reconstitute(
                userId,
                tenantId,
                "user@example.com",
                "old-password-hash",
                "Test",
                "User",
                null,
                com.logistics.identity.domain.valueobject.UserStatus.ACTIVE,
                true,
                null,
                now.minusSeconds(3600),
                now.minusSeconds(60),
                0);

        UUID sessionId = UUID.randomUUID();
        UserSession session = UserSession.create(
                sessionId,
                userId,
                now.minusSeconds(60),
                now.plusSeconds(3600),
                "127.0.0.1",
                "JUnit");

        RefreshToken refreshToken = RefreshToken.create(
                UUID.randomUUID(),
                sessionId,
                "stored-refresh-token-hash",
                now.minusSeconds(30),
                now.plusSeconds(1800));

        when(resetTokenRepository.findByTokenHashForUpdate(tokenHash))
                .thenReturn(Optional.of(resetToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.hash("NewStrongPassword123!"))
                .thenReturn("new-password-hash");
        when(sessionRepository.findByUserId(userId)).thenReturn(List.of(session));
        when(refreshTokenRepository.findBySessionId(sessionId))
                .thenReturn(List.of(refreshToken));

        service.resetPassword(rawToken, "NewStrongPassword123!");

        assertEquals("new-password-hash", user.passwordHash());
        assertNotNull(resetToken.consumedAt());
        assertTrue(session.isRevoked());
        assertTrue(refreshToken.isRevoked());

        verify(userRepository).save(user);
        verify(resetTokenRepository).save(resetToken);
        verify(resetTokenRepository).revokeActiveByUserId(userUuid, now);
        verify(sessionRepository).save(session);
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void shouldRejectUnknownToken() {
        String rawToken = tokenGenerator.generateToken();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        when(resetTokenRepository.findByTokenHashForUpdate(tokenHash))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.resetPassword(rawToken, "NewStrongPassword123!"));

        verifyNoInteractions(userRepository);
        verify(sessionRepository, never()).findByUserId(any());
    }

    @Test
    void shouldRejectExpiredToken() {
        String rawToken = tokenGenerator.generateToken();

        PasswordResetToken expiredToken = PasswordResetToken.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                tokenGenerator.hashToken(rawToken),
                now.minusSeconds(1800),
                now);

        when(resetTokenRepository.findByTokenHashForUpdate(
                        tokenGenerator.hashToken(rawToken)))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.resetPassword(rawToken, "NewStrongPassword123!"));

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectBlankResetToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.resetPassword("  ", "NewStrongPassword123!"));

        verifyNoInteractions(resetTokenRepository);
    }

    @Test
    void shouldRejectInvalidNewPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.resetPassword("some-token", "short"));

        verifyNoInteractions(resetTokenRepository);
    }
}