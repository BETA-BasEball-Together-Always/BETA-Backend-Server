package com.beta.account.infra.repository;

import com.beta.core.security.AdminAuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminRefreshTokenRedisRepository {

    private final StringRedisTemplate redisTemplate;

    public void save(Long userId, String refreshToken) {
        deleteByUserId(userId);

        String tokenKey = AdminAuthConstants.REFRESH_TOKEN_TOKEN_KEY_PREFIX + refreshToken;
        String userKey = AdminAuthConstants.REFRESH_TOKEN_USER_KEY_PREFIX + userId;

        redisTemplate.opsForValue().set(tokenKey, String.valueOf(userId), AdminAuthConstants.REFRESH_TOKEN_TTL);
        redisTemplate.opsForValue().set(userKey, refreshToken, AdminAuthConstants.REFRESH_TOKEN_TTL);
    }

    public Optional<Long> findUserIdByToken(String refreshToken) {
        String tokenKey = AdminAuthConstants.REFRESH_TOKEN_TOKEN_KEY_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(tokenKey);
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(userId));
    }

    public void deleteByUserId(Long userId) {
        String userKey = AdminAuthConstants.REFRESH_TOKEN_USER_KEY_PREFIX + userId;
        String refreshToken = redisTemplate.opsForValue().get(userKey);

        if (refreshToken != null) {
            String tokenKey = AdminAuthConstants.REFRESH_TOKEN_TOKEN_KEY_PREFIX + refreshToken;
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(userKey);
            log.debug("Deleted admin refresh token - userId: {}", userId);
        }
    }
}
