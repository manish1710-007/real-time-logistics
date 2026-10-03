package com.logistics.identity.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties properties;
    private final Key signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(
            String userId, String tenantId, Collection<String> roles, Collection<String> permissions) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getAccessTokenTtl());

        return Jwts.builder()
                .issuer(properties.getIssuer())
                .audience()
                .add(properties.getAudience())
                .and()
                .setSubject(userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("tenantId", tenantId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .signWith(signingKey)
                .compact();
    }

    public Claims validateAccessToken(String token) {
        Jws<Claims> parsed = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token);

        Claims claims = parsed.getPayload();

        if (claims.getAudience() == null || !claims.getAudience().contains(properties.getAudience())) {
            throw new IllegalArgumentException("JWT audience does not match");
        }

        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new IllegalArgumentException("JWT subject is missing");
        }

        if (claims.get("tenant_id", String.class) == null
                || claims.get("tenant_id", String.class).isBlank()) {
            throw new IllegalArgumentException("JWT tenant_id is missing");
        }

        return claims;
    }
}
