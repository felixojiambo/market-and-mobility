package com.cymelle.app.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenStore {

    private final StringRedisTemplate redis;

    @Value("${app.jwt.refresh-expiry-days}")
    private long refreshExpiryDays;

    private static final String PREFIX = "refresh:";

    /**
     * Creates a refresh token, stores mapping token -> userId with TTL, and returns token.
     * Rotation strategy: on refresh, we delete the old token and issue a new one.
     */
    public String issue(Long userId) {
        String token = UUID.randomUUID().toString();

        Duration ttl = Duration.ofDays(refreshExpiryDays);
        redis.opsForValue().set(PREFIX + token, String.valueOf(userId), ttl);

        return token;
    }

    public Optional<Long> getUserIdIfValid(String refreshToken) {
        String value = redis.opsForValue().get(PREFIX + refreshToken);
        if (value == null) return Optional.empty();
        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public void revoke(String refreshToken) {
        redis.delete(PREFIX + refreshToken);
    }
}
