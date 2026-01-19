package com.cymelle.app.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenStore {

    private final StringRedisTemplate redis;

    @Value("${app.jwt.refresh-expiry-days}")
    private long refreshExpiryDays;

    private static final String TOKEN_PREFIX = "refresh:";
    private static final String VERSION_PREFIX = "refresh:ver:";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String USED_PREFIX = "refresh:used:";

    public boolean markUsedOnce(String refreshToken) {
        // returns true if we successfully marked it as used for the first time
        Boolean ok = redis.opsForValue().setIfAbsent(USED_PREFIX + refreshToken, "1", Duration.ofMinutes(10));
        return Boolean.TRUE.equals(ok);
    }

    public void forgetUsedMarker(String refreshToken) {
        redis.delete(USED_PREFIX + refreshToken);
    }

    private Duration ttl() {
        return Duration.ofDays(refreshExpiryDays);
    }

    /**
     * Issue a new opaque refresh token.
     * Stored as: refresh:<token> -> "userId:version" with TTL.
     */
    public String issue(Long userId) {
        int version = getOrInitVersion(userId);
        String token = generateToken();

        String value = userId + ":" + version;
        redis.opsForValue().set(TOKEN_PREFIX + token, value, ttl());

        return token;
    }

    /**
     * Validate token and return userId if valid.
     */
    public Optional<Long> getUserIdIfValid(String refreshToken) {
        String value = redis.opsForValue().get(TOKEN_PREFIX + refreshToken);
        if (value == null || value.isBlank()) return Optional.empty();

        String[] parts = value.split(":");
        if (parts.length != 2) return Optional.empty();

        try {
            Long userId = Long.parseLong(parts[0]);
            int tokenVersion = Integer.parseInt(parts[1]);

            int currentVersion = getOrInitVersion(userId);
            if (tokenVersion != currentVersion) return Optional.empty();

            return Optional.of(userId);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /**
     * Revoke a single refresh token.
     */
    public void revoke(String refreshToken) {
        redis.delete(TOKEN_PREFIX + refreshToken);
    }

    /**
     * Revoke ALL refresh tokens for a user (logout everywhere).
     * Implementation: increment version so all existing tokens become invalid.
     * Also refresh TTL to keep it tidy.
     */
    public void revokeAllForUser(Long userId) {
        String key = VERSION_PREFIX + userId;
        redis.opsForValue().increment(key);
        redis.expire(key, ttl());
    }

    private int getOrInitVersion(Long userId) {
        String key = VERSION_PREFIX + userId;

        // setIfAbsent prevents race conditions on first init
        Boolean created = redis.opsForValue().setIfAbsent(key, "1", ttl());
        if (Boolean.TRUE.equals(created)) {
            return 1;
        }

        // already exists: parse it
        String value = redis.opsForValue().get(key);
        if (value == null) {
            // very rare edge: expired between calls, recreate
            redis.opsForValue().set(key, "1", ttl());
            return 1;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // repair corrupt value
            redis.opsForValue().set(key, "1", ttl());
            return 1;
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
