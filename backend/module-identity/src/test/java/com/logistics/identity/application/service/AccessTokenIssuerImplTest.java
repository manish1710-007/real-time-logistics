package com.logistics.identity.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.valueobject.PermissionId;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.RoleType;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.jwt.JwtTokenService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessTokenIssuerImplTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-01-01T12:00:00Z");

    private JwtTokenService jwtTokenService;
    private AccessTokenIssuerImpl issuer;

    @BeforeEach
    void setUp() {
        jwtTokenService = org.mockito.Mockito.mock(JwtTokenService.class);

        Clock clock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

        issuer = new AccessTokenIssuerImpl(jwtTokenService, clock);
    }

    @Test
    void shouldIssueAccessTokenWithCorrectIdentityAndAuthorizationData() {
        UserId userId = UserId.generate();
        TenantId tenantId = TenantId.generate();

        Role role = Role.reconstitute(
                RoleId.generate(), tenantId, "ADMIN", RoleType.TENANT, Set.of(), FIXED_TIME, FIXED_TIME);

        Permission permission = Permission.reconstitute(PermissionId.generate(), "USER_READ", "Read users");

        String expectedToken = "access-token";

        when(jwtTokenService.issueAccessToken(
                        userId.value().toString(), tenantId.value().toString(), List.of("ADMIN"), List.of("USER_READ")))
                .thenReturn(expectedToken);

        when(jwtTokenService.accessTokenExpiresAt(FIXED_TIME)).thenReturn(FIXED_TIME.plusSeconds(900));

        IssuedAccessToken result = issuer.issue(userId, tenantId, List.of(role), List.of(permission));

        assertNotNull(result);
        assertEquals(expectedToken, result.token());
        assertEquals(FIXED_TIME.plusSeconds(900), result.expiresAt());

        verify(jwtTokenService)
                .issueAccessToken(
                        userId.value().toString(), tenantId.value().toString(), List.of("ADMIN"), List.of("USER_READ"));

        verify(jwtTokenService).accessTokenExpiresAt(FIXED_TIME);
    }
}
