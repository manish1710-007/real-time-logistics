package com.logistics.identity.application.service;

import com.logistics.identity.domain.repository.PasswordResetTokenRepository;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.service.PasswordHasher;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.infrastructure.security.RefreshTokenGenerator;
import java.time.Clock;
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}