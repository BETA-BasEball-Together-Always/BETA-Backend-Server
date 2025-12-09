package com.beta.account.infra.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_KEY_PREFIX = "refresh_token:token:";
    private static final String USER_KEY_PREFIX = "refresh_token:user:";
    private static final Duration TTL = Duration.ofDays(30);

    public void save(Long userId, String refreshToken) {
        deleteByUserId(userId);

        String tokenKey = TOKEN_KEY_PREFIX + refreshToken;
        String userKey = USER_KEY_PREFIX + userId;

        redisTemplate.opsForValue().set(tokenKey, String.valueOf(userId), TTL);
        redisTemplate.opsForValue().set(userKey, refreshToken, TTL);
    }

    public Optional<Long> findUserIdByToken(String refreshToken) {
        String tokenKey = TOKEN_KEY_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(tokenKey);
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(userId));
    }

    public void deleteByUserId(Long userId) {
        String userKey = USER_KEY_PREFIX + userId;
        String refreshToken = redisTemplate.opsForValue().get(userKey);

        if (refreshToken != null) {
            String tokenKey = TOKEN_KEY_PREFIX + refreshToken;
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(userKey);
            log.debug("Deleted refresh token - userId: {}", userId);
        }
    }
}
