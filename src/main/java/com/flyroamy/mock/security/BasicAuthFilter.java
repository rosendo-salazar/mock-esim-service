package com.flyroamy.mock.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class BasicAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(BasicAuthFilter.class);

    @Value("${mock.auth.api-key}")
    private String apiKey;

    @Value("${mock.auth.api-secret}")
    private String apiSecret;

    private static final List<String> PUBLIC_PATHS = List.of(
        "/v1/admin/health",
        "/actuator",
        "/swagger-ui",
        "/api-docs",
        "/v3/api-docs",
        "/qr/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip authentication for public endpoints
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header");
            return;
        }

        try {
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);

            if (parts.length != 2) {
                logger.warn("Invalid credentials format for path: {}", path);
                sendUnauthorizedResponse(response, "Invalid credentials format");
                return;
            }

            String providedKey = parts[0];
            String providedSecret = parts[1];

            if (!apiKey.equals(providedKey) || !apiSecret.equals(providedSecret)) {
                logger.warn("Invalid API credentials for path: {}", path);
                sendUnauthorizedResponse(response, "Invalid API credentials");
                return;
            }

            // Authentication successful
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                providedKey,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Authenticated request for path: {}", path);
            filterChain.doFilter(request, response);

        } catch (IllegalArgumentException e) {
            logger.error("Error decoding credentials: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Invalid credentials encoding");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("""
            {
                "error": {
                    "code": "AUTHENTICATION_FAILED",
                    "message": "%s",
                    "timestamp": "%s"
                }
            }
            """, message, java.time.Instant.now().toString()));
    }
}
