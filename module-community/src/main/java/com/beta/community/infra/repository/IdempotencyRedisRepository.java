package com.beta.community.infra.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IdempotencyRedisRepository {

    private final StringRedisTemplate stringRedisTemplate;

    private static final Duration TTL = Duration.ofSeconds(30);

    public boolean setIfAbsent(String key) {
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", TTL);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("Redis 멱등성 체크 실패, fail-open 처리: key={}", key, e);
            return true;
        }
    }
}
