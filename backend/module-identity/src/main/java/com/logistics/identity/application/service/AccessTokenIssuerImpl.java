package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.jwt.JwtTokenService;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenIssuerImpl implements AccessTokenIssuer {

    private final JwtTokenService jwtTokenService;

    public AccessTokenIssuerImpl(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public IssuedAccessToken issue(UserId userId, TenantId tenantId, List<Role> roles, List<Permission> permissions) {

        Instant issuedAt = Instant.now();

        String token = jwtTokenService.issueAccessToken(
                userId.value().toString(),
                tenantId.value().toString(),
                roles.stream().map(Role::name).toList(),
                permissions.stream().map(Permission::code).toList());

        return new IssuedAccessToken(token, jwtTokenService.accessTokenExpiresAt(issuedAt));
    }
}
