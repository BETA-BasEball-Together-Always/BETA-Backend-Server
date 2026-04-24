package com.beta.community.infra.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationThrottleRedisRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryAcquire(String key, Duration ttl) {
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("Redis 알림 스로틀 체크 실패, fail-closed 처리: key={}", key, e);
            return false;
        }
    }
}
