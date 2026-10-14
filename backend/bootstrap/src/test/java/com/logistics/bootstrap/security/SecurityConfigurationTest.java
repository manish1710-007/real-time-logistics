
package com.logistics.bootstrap.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logistics.identity.infrastructure.jwt.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
        classes = SecurityConfigurationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenService jwtTokenService;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({SecurityConfiguration.class, TestEndpoints.class})
    static class TestApplication {
    }

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
    }

    @Test
    void healthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void otherEndpointsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized());
    }
}
