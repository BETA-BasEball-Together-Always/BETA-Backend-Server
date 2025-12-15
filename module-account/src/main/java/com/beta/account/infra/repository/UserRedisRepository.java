package com.beta.account.infra.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class UserRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String PASSWORD_CODE_PREFIX = "password:code:";
    private static final String PASSWORD_CODE_COOLDOWN_PREFIX = "password:code:cooldown:";
    private static final Duration TTL = Duration.ofMinutes(2);
    private static final Duration COOLDOWN_TTL = Duration.ofMinutes(1);

    public void savePasswordCode(Long userId, String code) {
        String key = PASSWORD_CODE_PREFIX + userId;
        redisTemplate.opsForValue().set(key, code, TTL);
    }

    public String getPasswordCode(Long userId) {
        String key = PASSWORD_CODE_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deletePasswordCode(Long userId) {
        String key = PASSWORD_CODE_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public void saveCooldown(Long userId) {
        String key = PASSWORD_CODE_COOLDOWN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, "1", COOLDOWN_TTL);
    }

    public boolean isCooldownActive(Long userId) {
        String key = PASSWORD_CODE_COOLDOWN_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Long getCooldownTTL(Long userId) {
        String key = PASSWORD_CODE_COOLDOWN_PREFIX + userId;
        return redisTemplate.getExpire(key);
    }
}
