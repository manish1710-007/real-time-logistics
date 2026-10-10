package com.logistics.identity.application.service;

import com.logistics.identity.application.command.RefreshTokenCommand;
import com.logistics.identity.application.dto.RefreshTokenResult;
import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.TenantRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.valueobject.TenantStatus;
import com.logistics.identity.domain.valueobject.UserStatus;
import com.logistics.identity.infrastructure.security.RefreshTokenGenerator;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final UserRepository userRepository;
    private final RoleLoader roleLoader;
    private final PermissionLoader permissionLoader;
    private final AccessTokenIssuer accessTokenIssuer;
    private final TenantRepository tenantRepository;
    private final Clock clock;

    private void revokeSession(UserSession session, Instant now) {
        session.revoke(now);
        userSessionRepository.save(session);
    }

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            UserSessionRepository userSessionRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            RoleLoader roleLoader,
            PermissionLoader permissionLoader,
            AccessTokenIssuer accessTokenIssuer,
            Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userSessionRepository = userSessionRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.roleLoader = roleLoader;
        this.permissionLoader = permissionLoader;
        this.accessTokenIssuer = accessTokenIssuer;
        this.clock = clock;
    }

    @Override
    @Transactional(noRollbackFor = RefreshTokenReuseDetectedException.class)
    public RefreshTokenResult refreshToken(RefreshTokenCommand command) {
        Instant now = clock.instant();

        String tokenHash = refreshTokenGenerator.hashToken(command.refreshToken());

        RefreshToken currentToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        UserSession session = userSessionRepository
                .findByIdForUpdate(currentToken.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token session not found"));

        if (!session.isActive(now)) {
            throw new IllegalArgumentException("Refresh token session is inactive");
        }

        if (currentToken.isExpired(now)) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        if (currentToken.isRevoked() || currentToken.isReplaced()) {
            session.revoke(now);
            userSessionRepository.save(session);

            throw new RefreshTokenReuseDetectedException();
        }

        var user = userRepository
                .findById(session.userId())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token user not found"));

        if (user.status() != UserStatus.INVITED && user.status() != UserStatus.ACTIVE) {
            revokeSession(session, now);
            throw new IllegalArgumentException("Refresh token user is inactive");
        }

        tenantRepository
                .findById(user.tenantId())
                .filter(t -> t.status() == TenantStatus.ACTIVE)
                .orElseThrow(() -> {
                    revokeSession(session, now);
                    throw new IllegalArgumentException("Refresh token tenant is inactive");
                });

        var roles = roleLoader.loadRole(user.id());
        var permissions = permissionLoader.loadPermissions(roles);

        IssuedAccessToken accessToken = accessTokenIssuer.issue(user.id(), user.tenantId(), roles, permissions);

        String newRawToken = refreshTokenGenerator.generateToken();
        String newTokenHash = refreshTokenGenerator.hashToken(newRawToken);

        UUID newTokenId = UUID.randomUUID();

        currentToken.replaceWith(newTokenId, now);
        refreshTokenRepository.save(currentToken);

        Instant newRefreshTokenExpiresAt = currentToken.expiresAt();

        RefreshToken replacement =
                RefreshToken.create(newTokenId, currentToken.sessionId(), newTokenHash, now, newRefreshTokenExpiresAt);

        refreshTokenRepository.save(replacement);

        return new RefreshTokenResult(
                accessToken.token(), newRawToken, accessToken.expiresAt(), newRefreshTokenExpiresAt);
    }
}
