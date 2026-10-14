
package com.logistics.bootstrap.security;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.logistics.identity.infrastructure.jwt.JwtTokenService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenBearerTokenIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldRejectInvalidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtTokenService.validateAccessToken("invalid-token"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals(null, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateValidBearerTokenAndMapAuthorities() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(jwtTokenService.validateAccessToken("valid-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user-123");
        when(claims.get("roles", List.class)).thenReturn(List.of("ADMIN"));
        when(claims.get("permissions", List.class)).thenReturn(List.of("order:read"));

        filter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals("user-123", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("PERM_order:read")));

        verify(filterChain).doFilter(request, response);
    }
}
