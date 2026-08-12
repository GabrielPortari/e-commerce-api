package com.ecommerce.gabrielportari.e_commerce_api.config.ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * In-memory fixed-window rate limiter, keyed by an arbitrary string (e.g.
 * "login:" + clientIp). Single-instance only — if the API ever runs with
 * multiple replicas behind a load balancer, this should move to a shared
 * store (e.g. Redis) instead.
 */
@Component
public class RateLimiter {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key, int maxAttempts, Duration windowDuration) {
        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.startMillis() > windowDuration.toMillis()) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });
        return window.count().get() <= maxAttempts;
    }

    private record Window(long startMillis, AtomicInteger count) {}
}
