package com.logistics.bootstrap.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logistics.identity.infrastructure.jwt.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
        classes = SecurityConfigurationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"management.health.mail.enabled=false"})
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenService jwtTokenService;

    @SpringBootConfiguration
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                FlywayAutoConfiguration.class
            })
    @Import({SecurityConfiguration.class, TestEndpoints.class})
    static class TestApplication {}

    @RestController
    static class TestEndpoints {

        @GetMapping("/actuator/health")
        String health() {
            return "UP";
        }

        @GetMapping("/api/protected")
        String protectedEndpoint() {
            return "protected";
        }

        @PreAuthorize("hasAuthority('PERM_order:read')")
        @GetMapping("/api/orders")
        String readOrders() {
            return "orders";
        }
    }

    @Test
    void healthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void otherEndpointsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/protected")).andExpect(status().isUnauthorized());
    }

    @Test
    void permissionProtectedEndpointShouldRejectMissingPermission() throws Exception {
        mockMvc.perform(get("/api/orders").with(SecurityMockMvcRequestPostProcessors.user("test-user")))
                .andExpect(status().isForbidden());
    }

    @Test
    void permissionProtectedEndpointShouldAllowRequiredPermission() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        "test-user",
                                        null,
                                        java.util.List.of(new SimpleGrantedAuthority("PERM_order:read"))))))
                .andExpect(status().isOk());
    }
}
