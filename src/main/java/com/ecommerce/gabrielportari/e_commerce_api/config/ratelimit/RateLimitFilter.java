package com.ecommerce.gabrielportari.e_commerce_api.config.ratelimit;

import com.ecommerce.gabrielportari.e_commerce_api.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Throttles brute-force-prone public endpoints (login, review submission) by
 * client IP. Uses request.getRemoteAddr() rather than X-Forwarded-For, since
 * that header is client-controlled unless a trusted proxy strips/sets it; if
 * the app is deployed behind a reverse proxy, enable
 * server.forward-headers-strategy=native so getRemoteAddr() reflects the
 * real client IP.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Pattern REVIEW_PATH = Pattern.compile("^/api/products/[^/]+/reviews$");

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = request.getRemoteAddr();

        if ("POST".equals(method) && "/api/auth/login".equals(uri)) {
            if (!rateLimiter.tryAcquire("login:" + clientIp, 5, Duration.ofMinutes(1))) {
                respondTooManyRequests(response, request, "Muitas tentativas de login. Tente novamente em instantes.");
                return;
            }
        } else if ("POST".equals(method) && REVIEW_PATH.matcher(uri).matches()) {
            if (!rateLimiter.tryAcquire("review:" + clientIp, 10, Duration.ofHours(1))) {
                respondTooManyRequests(response, request, "Muitas avaliações enviadas. Tente novamente mais tarde.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void respondTooManyRequests(HttpServletResponse response, HttpServletRequest request, String message)
            throws IOException {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
