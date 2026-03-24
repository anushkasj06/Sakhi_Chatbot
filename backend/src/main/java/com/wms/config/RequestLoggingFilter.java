package com.wms.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private static final Set<String> SENSITIVE_QUERY_KEYS = Set.of(
        "password",
        "passwordhash",
        "token",
        "refreshtoken",
        "authorization",
        "secret"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        long start = System.currentTimeMillis();

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = sanitizeQuery(request.getQueryString());
        String clientIp = resolveClientIp(request);

        log.info("request_started method={} path={} query={} clientIp={}", method, path, query, clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String user = resolveCurrentUser();
            MDC.put("user", user);

            log.info(
                "request_completed method={} path={} status={} durationMs={} user={}",
                method,
                path,
                status,
                durationMs,
                user
            );

            MDC.remove("user");
            MDC.remove("correlationId");
        }
    }

    private String resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String sanitizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        return Arrays.stream(query.split("&"))
            .map(this::maskQueryPart)
            .reduce((left, right) -> left + "&" + right)
            .orElse("");
    }

    private String maskQueryPart(String part) {
        String[] keyValue = part.split("=", 2);
        String key = keyValue[0];
        if (SENSITIVE_QUERY_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
            return key + "=***";
        }
        return part;
    }
}
