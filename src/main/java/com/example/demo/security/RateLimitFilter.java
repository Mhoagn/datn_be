package com.example.demo.security;

import com.example.demo.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

import static java.time.Duration.ofMinutes;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<String> rateLimitProxyManager;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${app.rate-limit.authenticated-per-minute:120}")
    private long authenticatedPerMinute;

    @Value("${app.rate-limit.anonymous-per-minute:60}")
    private long anonymousPerMinute;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }

        return path.startsWith("/actuator")
                || path.startsWith("/webhook")
                || path.startsWith("/ws");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String userId = resolveUserId(request);
            final String bucketKey;
            final long capacity;

            if (userId != null) {
                bucketKey = "rl:user:" + userId;
                capacity = authenticatedPerMinute;
            } else {
                bucketKey = "rl:ip:" + resolveClientIp(request);
                capacity = anonymousPerMinute;
            }

            BucketConfiguration configuration = BucketConfiguration.builder()
                    .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, ofMinutes(1)))
                    .build();

            Bucket bucket = rateLimitProxyManager.getProxy(bucketKey, () -> configuration);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                long waitSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);
                writeTooManyRequests(request, response, waitSeconds);
                return;
            }
        } catch (Exception e) {
            // Fail-open: nếu Redis/Bucket4j lỗi thì không chặn request
            log.warn("Rate limit unavailable, allowing request: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        try {
            return jwtService.parseAccess(header.substring(7)).getPayload().getSubject();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private void writeTooManyRequests(
            HttpServletRequest request,
            HttpServletResponse response,
            long retryAfterSeconds
    ) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.builder()
                .message("Bạn đang gửi quá nhiều yêu cầu. Vui lòng thử lại sau.")
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }
}
