
package com.logistics.bootstrap.identity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.logistics.bootstrap.LogisticsApplication;
import com.logistics.identity.application.command.RefreshTokenCommand;
import com.logistics.identity.application.service.InactiveIdentityRefreshException;
import com.logistics.identity.application.service.RefreshTokenService;
import com.logistics.identity.domain.entity.RefreshToken;
import com.logistics.identity.domain.entity.Tenant;
import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.repository.RefreshTokenRepository;
import com.logistics.identity.domain.repository.TenantRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.repository.UserSessionRepository;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.TenantStatus;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.domain.valueobject.UserStatus;
import com.logistics.identity.infrastructure.security.RefreshTokenGenerator;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
        classes = LogisticsApplication.class,
        properties = {
            "identity.jwt.issuer=logistics-integration-test",
            "identity.jwt.audience=logistics-integration-test",
            "identity.jwt.secret=test-only-jwt-secret-0123456789-ABCDEF",
            "identity.jwt.access-token-ttl=PT15M",
            "spring.flyway.enabled=true",
            "spring.flyway.locations=classpath:db/migration",
            "spring.jpa.hibernate.ddl-auto=validate"
        })
class RefreshTokenRevocationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("logistics_test")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenGenerator refreshTokenGenerator;

    @Test
    void shouldCommitSessionRevocationWhenUserIsInactive() {
        Instant now = Instant.now();

        TenantId tenantId = TenantId.generate();
        UserId userId = UserId.generate();
        UUID sessionId = UUID.randomUUID();
        UUID refreshTokenId = UUID.randomUUID();

        // 1. Persist an active tenant.
        Tenant tenant = Tenant.create(
                tenantId,
                "Integration Test Tenant",
                "integration-test-" + UUID.randomUUID());

        tenantRepository.save(tenant);

        // 2. Persist a suspended user belonging to that tenant.
        User user = User.reconstitute(
                userId,
                tenantId,
                "inactive-" + UUID.randomUUID() + "@example.test",
                "test-password-hash",
                "Test",
                "User",
                null,
                UserStatus.SUSPENDED,
                false,
                null,
                now,
                now,
                0);

        userRepository.save(user);

        // 3. Persist an active session for the suspended user.
        UserSession session = UserSession.create(
                sessionId,
                userId,
                now,
                now.plusSeconds(3600),
                "127.0.0.1",
                "integration-test");

        userSessionRepository.save(session);

        // 4. Persist only the hash of the refresh token.
        String rawRefreshToken = refreshTokenGenerator.generateToken();
        String tokenHash = refreshTokenGenerator.hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.create(
                refreshTokenId,
                sessionId,
                tokenHash,
                now,
                now.plusSeconds(3600));

        refreshTokenRepository.save(refreshToken);

        // 5. Refresh must fail and revoke the user's session.
        assertThrows(
                InactiveIdentityRefreshException.class,
                () -> refreshTokenService.refreshToken(
                        new RefreshTokenCommand(rawRefreshToken)));

        // 6. Read through the repository after the service transaction ended.
        UserSession persistedSession = userSessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new AssertionError("Session was not persisted"));

        assertNotNull(
                persistedSession.revokedAt(),
                "Session revocation must be committed despite the exception");
    }
}
