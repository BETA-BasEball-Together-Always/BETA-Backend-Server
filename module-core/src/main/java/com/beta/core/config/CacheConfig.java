package com.beta.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String KBO_RANKING_CACHE = "kboRanking";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(KBO_RANKING_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(calculateTtlUntilMidnight())
                .maximumSize(10));
        return cacheManager;
    }

    private Duration calculateTtlUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        Duration ttl = Duration.between(now, midnight);
        return ttl.isNegative() || ttl.isZero() ? Duration.ofHours(24) : ttl;
    }
}
