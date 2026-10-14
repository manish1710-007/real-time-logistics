package com.logistics.identity.infrastructure.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private static final String SECRET = "test-only-jwt-secret-0123456789-ABCDEF";
    private static final Instant NOW = Instant.parse("2026-10-14T00:00:00Z");

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("logistics-test");
        properties.setAudience("logistics-api");
        properties.setSecret(SECRET);
        properties.setAccessTokenTtl(Duration.ofMinutes(15));

        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        jwtTokenService = new JwtTokenService(properties, clock);
    }

    @Test
    void shouldIssueAndValidateAccessToken() {
        String token =
                jwtTokenService.issueAccessToken("user-123", "tenant-456", List.of("ADMIN"), List.of("order:read"));

        Claims claims = jwtTokenService.validateAccessToken(token);

        assertEquals("user-123", claims.getSubject());
        assertEquals("logistics-test", claims.getIssuer());
        assertTrue(claims.getAudience().contains("logistics-api"));
        assertEquals("tenant-456", claims.get("tenant_id", String.class));
        assertEquals(List.of("ADMIN"), claims.get("roles", List.class));
        assertEquals(List.of("order:read"), claims.get("permissions", List.class));
        assertEquals(NOW.plus(Duration.ofMinutes(15)), claims.getExpiration().toInstant());
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        String token = jwtTokenService.issueAccessToken("user-123", "tenant-456", List.of(), List.of());

        JwtProperties otherProperties = new JwtProperties();
        otherProperties.setIssuer("logistics-test");
        otherProperties.setAudience("logistics-api");
        otherProperties.setSecret("different-test-secret-0123456789-XYZ");
        otherProperties.setAccessTokenTtl(Duration.ofMinutes(15));

        JwtTokenService otherService = new JwtTokenService(otherProperties, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(JwtException.class, () -> otherService.validateAccessToken(token));
    }

    @Test
    void shouldRejectTokenWithWrongIssuer() {
        String token = jwtTokenService.issueAccessToken("user-123", "tenant-456", List.of(), List.of());

        JwtProperties wrongIssuerProperties = new JwtProperties();
        wrongIssuerProperties.setIssuer("wrong-issuer");
        wrongIssuerProperties.setAudience("logistics-api");
        wrongIssuerProperties.setSecret(SECRET);
        wrongIssuerProperties.setAccessTokenTtl(Duration.ofMinutes(15));

        JwtTokenService wrongIssuerService =
                new JwtTokenService(wrongIssuerProperties, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(JwtException.class, () -> wrongIssuerService.validateAccessToken(token));
    }
}
