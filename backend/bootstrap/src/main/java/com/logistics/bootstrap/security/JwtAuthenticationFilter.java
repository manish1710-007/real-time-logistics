package com.logistics.bootstrap.security;

import com.logistics.identity.infrastructure.jwt.JwtTokenService;
import com.logistics.shared.security.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtTokenService.validateAccessToken(token);
            String tenantId = claims.get("tenant_id", String.class);
            TenantContext.setTenantId(UUID.fromString(tenantId));
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            addAuthorities(authorities, claims.get("roles", List.class), "ROLE_");
            addAuthorities(authorities, claims.get("permissions", List.class), "PERM_");

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid bearer token");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void addAuthorities(Collection<SimpleGrantedAuthority> authorities, List<?> values, String prefix) {

        if (values == null) {
            return;
        }

        for (Object value : values) {
            if (value instanceof String name && !name.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(prefix + name));
            }
        }
    }
}
