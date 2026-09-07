package com.logistics.bootstrap.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String traceId = request.getHeader("X-Trace-Id");
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }

            String correlationId = request.getHeader("X-Correlation-Id");
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = traceId;
            }

            String requestId = UUID.randomUUID().toString();

            MDC.put(TRACE_ID, traceId);
            MDC.put(CORRELATION_ID, correlationId);
            MDC.put(REQUEST_ID, requestId);

            response.setHeader("X-Trace-Id", traceId);
            response.setHeader("X-Correlation-Id", correlationId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear(); // Critical: prevent thread-local leaks
        }
    }
}