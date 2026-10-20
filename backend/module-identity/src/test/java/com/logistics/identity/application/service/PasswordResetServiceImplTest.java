package com.logistics.identity.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.logistics.identity.domain.valueobject.UserStatus;
import com.logistics.identity.infrastructure.security.RefreshTokenGenerator;

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


    @Test
    void shouldRequestPasswordResetForActiveUser() {
        TenantId tenantId = TenantId.generate();
        User user = createTestUser(
                tenantId, "user@example.com", UserStatus.ACTIVE);

        when(userRepository.findByTenantIdAndEmail(
                tenantId, "user@example.com"))
                .thenReturn(Optional.of(user));

        service.requestPasswordReset(tenantId, "user@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertEquals(user.id().value(), savedToken.userId());
        assertEquals(now, savedToken.createdAt());
        assertEquals(now.plusSeconds(900), savedToken.expiresAt());

        ArgumentCaptor<String> rawTokenCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Instant> expiryCaptor =
                ArgumentCaptor.forClass(Instant.class);

        verify(emailSender).sendPasswordResetEmail(
                eq("user@example.com"),
                rawTokenCaptor.capture(),
                expiryCaptor.capture());

        String rawToken = rawTokenCaptor.getValue();
        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());
        assertNotEquals(rawToken, savedToken.tokenHash());
        assertEquals(
                tokenGenerator.hashToken(rawToken),
                savedToken.tokenHash());
        assertEquals(now.plusSeconds(900), expiryCaptor.getValue());

        InOrder order = inOrder(resetTokenRepository, emailSender);
        order.verify(resetTokenRepository)
                .revokeActiveByUserId(user.id().value(), now);
        order.verify(resetTokenRepository).save(any(PasswordResetToken.class));
        order.verify(emailSender).sendPasswordResetEmail(
                eq("user@example.com"), eq(rawToken), eq(now.plusSeconds(900)));
    }

    @Test
    void shouldNotCreateResetTokenForUnknownEmail() {
        TenantId tenantId = TenantId.generate();

        when(userRepository.findByTenantIdAndEmail(
                tenantId, "unknown@example.com"))
                .thenReturn(Optional.empty());

        service.requestPasswordReset(tenantId, "unknown@example.com");

        verify(userRepository).findByTenantIdAndEmail(
                tenantId, "unknown@example.com");
        verifyNoInteractions(resetTokenRepository, emailSender);
    }

    @Test
    void shouldNotCreateResetTokenForInactiveUser() {
        TenantId tenantId = TenantId.generate();
        User user = createTestUser(
                tenantId, "inactive@example.com", UserStatus.SUSPENDED);

        when(userRepository.findByTenantIdAndEmail(
                tenantId, "inactive@example.com"))
                .thenReturn(Optional.of(user));

        service.requestPasswordReset(tenantId, "inactive@example.com");

        verify(userRepository).findByTenantIdAndEmail(
                tenantId, "inactive@example.com");
        verifyNoInteractions(resetTokenRepository, emailSender);
    }

    @Test
    void shouldNormalizeEmailBeforeLookup() {
        TenantId tenantId = TenantId.generate();
        User user = createTestUser(
                tenantId, "user@example.com", UserStatus.ACTIVE);

        when(userRepository.findByTenantIdAndEmail(
                tenantId, "user@example.com"))
                .thenReturn(Optional.of(user));

        service.requestPasswordReset(
                tenantId, "  USER@EXAMPLE.COM  ");

        verify(userRepository).findByTenantIdAndEmail(
                tenantId, "user@example.com");
        verify(resetTokenRepository)
                .revokeActiveByUserId(user.id().value(), now);
        verify(resetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailSender).sendPasswordResetEmail(
                eq("user@example.com"), any(String.class), eq(now.plusSeconds(900)));
    }

    private User createTestUser(
            TenantId tenantId, String email, UserStatus status) {
        return User.reconstitute(
                new UserId(UUID.randomUUID()),
                tenantId,
                email,
                "old-password-hash",
                "Test",
                "User",
                null,
                status,
                true,
                null,
                now.minusSeconds(3600),
                now.minusSeconds(60),
                0);
    }

}