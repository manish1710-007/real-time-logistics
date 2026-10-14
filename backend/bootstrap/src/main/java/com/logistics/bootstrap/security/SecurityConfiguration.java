package com.logistics.bootstrap.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.logistics.identity.infrastructure.jwt.JwtTokenService;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfiguration {

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        return http.csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Unauthorized"
                        )
                    )
                )    
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
