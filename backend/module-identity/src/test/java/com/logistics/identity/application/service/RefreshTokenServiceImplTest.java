package com.logistics.identity.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.infrastructure.security.RefreshTokenGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
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
                () -> refreshTokenService.refreshToken(
                        new com.logistics.identity.application.command.RefreshTokenCommand(rawToken)));

        assertEquals("Invalid refresh token", exception.getMessage());
    }
}
