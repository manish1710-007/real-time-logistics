package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.PasswordResetToken;
import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.repository.PasswordResetTokenRepository;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.service.PasswordHasher;
import com.logistics.identity.domain.service.PasswordPolicy;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.security.RefreshTokenGenerator;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator tokenGenerator;
    private final PasswordHasher passwordHasher;
    private final PasswordResetEmailSender emailSender;
    private final Clock clock;

    public PasswordResetServiceImpl(
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenGenerator tokenGenerator,
            PasswordHasher passwordHasher,
            PasswordResetEmailSender emailSender,
            Clock clock) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.passwordHasher = passwordHasher;
        this.emailSender = emailSender;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void requestPasswordReset(TenantId tenantId, String email) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        userRepository
                .findByTenantIdAndEmail(tenantId, normalizedEmail)
                .filter(user -> user.status() == com.logistics.identity.domain.valueobject.UserStatus.ACTIVE)
                .ifPresent(user -> {
                    Instant now = clock.instant();
                    Instant expiresAt = now.plusSeconds(15 * 60);

                    String rawToken = tokenGenerator.generateToken();
                    String tokenHash = tokenGenerator.hashToken(rawToken);

                    passwordResetTokenRepository.revokeActiveByUserId(user.id().value(), now);

                    PasswordResetToken resetToken = PasswordResetToken.create(
                            UUID.randomUUID(), user.id().value(), tokenHash, now, expiresAt);

                    passwordResetTokenRepository.save(resetToken);

                    emailSender.sendPasswordResetEmail(user.email(), rawToken, expiresAt);
                });
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Reset token is required");
        }

        PasswordPolicy.validate(newPassword);

        Instant now = clock.instant();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        if (!resetToken.isUsableAt(now)) {
            throw new IllegalArgumentException("Invalid or expired password reset token");
        }

        UserId userId = new UserId(resetToken.userId());

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        user.updatePasswordHash(passwordHasher.hash(newPassword));
        userRepository.save(user);

        resetToken.consume(now);
        passwordResetTokenRepository.save(resetToken);
        passwordResetTokenRepository.revokeActiveByUserId(userId.value(), now);

        // Revoke every session, including expired or previously revoked sessions.
        List<UserSession> sessions = userSessionRepository.findByUserId(userId);

        for (UserSession session : sessions) {
            session.revoke(now);
            userSessionRepository.save(session);

            List<RefreshToken> refreshTokens = refreshTokenRepository.findBySessionId(session.id());

            for (RefreshToken refreshToken : refreshTokens) {
                refreshToken.revoke(now);
                refreshTokenRepository.save(refreshToken);
            }
        }
    }
}
