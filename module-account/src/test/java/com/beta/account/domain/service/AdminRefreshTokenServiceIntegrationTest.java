package com.beta.account.domain.service;

import com.beta.account.infra.repository.AdminRefreshTokenRedisRepository;
import com.beta.core.security.AdminAuthConstants;
import com.beta.docker.MysqlRedisTestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AdminRefreshTokenServiceIntegrationTest extends MysqlRedisTestContainer {

    private AdminRefreshTokenService adminRefreshTokenService;
    private StringRedisTemplate redisTemplate;
    private LettuceConnectionFactory connectionFactory;

    @BeforeEach
    void set_up() {
        // given
        connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();

        AdminRefreshTokenRedisRepository adminRefreshTokenRedisRepository =
                new AdminRefreshTokenRedisRepository(redisTemplate);
        adminRefreshTokenService = new AdminRefreshTokenService(adminRefreshTokenRedisRepository);
    }

    @AfterEach
    void tear_down() {
        RedisConnection connection = connectionFactory.getConnection();
        connection.serverCommands().flushDb();
        connection.close();
        connectionFactory.destroy();
    }

    @Test
    void 관리자_리프레시_토큰을_저장하고_사용자_ID를_조회한다() {
        // given
        Long userId = 1L;
        String refreshToken = "admin-refresh-token-1";

        // when
        adminRefreshTokenService.upsertRefreshToken(userId, refreshToken);
        Long foundUserId = adminRefreshTokenService.findUserIdByToken(refreshToken);

        // then
        String tokenKey = AdminAuthConstants.REFRESH_TOKEN_TOKEN_KEY_PREFIX + refreshToken;
        String userKey = AdminAuthConstants.REFRESH_TOKEN_USER_KEY_PREFIX + userId;

        assertThat(foundUserId).isEqualTo(userId);
        assertThat(redisTemplate.hasKey(tokenKey)).isTrue();
        assertThat(redisTemplate.hasKey(userKey)).isTrue();
    }

    @Test
    void 관리자_리프레시_토큰_저장_시_기존_토큰이_새_토큰으로_교체된다() {
        // given
        Long userId = 7L;
        String oldToken = "admin-refresh-old";
        String newToken = "admin-refresh-new";

        // when
        adminRefreshTokenService.upsertRefreshToken(userId, oldToken);
        adminRefreshTokenService.upsertRefreshToken(userId, newToken);
        Long foundUserId = adminRefreshTokenService.findUserIdByToken(newToken);

        // then
        String oldTokenKey = AdminAuthConstants.REFRESH_TOKEN_TOKEN_KEY_PREFIX + oldToken;
        String newTokenKey = AdminAuthConstants.REFRESH_TOKEN_TOKEN_KEY_PREFIX + newToken;
        String userKey = AdminAuthConstants.REFRESH_TOKEN_USER_KEY_PREFIX + userId;

        assertThat(foundUserId).isEqualTo(userId);
        assertThat(redisTemplate.hasKey(oldTokenKey)).isFalse();
        assertThat(redisTemplate.hasKey(newTokenKey)).isTrue();
        assertThat(redisTemplate.opsForValue().get(userKey)).isEqualTo(newToken);
    }

    @Test
    void 사용자_ID로_관리자_리프레시_토큰을_삭제한다() {
        // given
        Long userId = 3L;
        String refreshToken = "admin-refresh-token-3";
        adminRefreshTokenService.upsertRefreshToken(userId, refreshToken);

        // when
        adminRefreshTokenService.deleteByUserId(userId);

        // then
        String tokenKey = AdminAuthConstants.REFRESH_TOKEN_TOKEN_KEY_PREFIX + refreshToken;
        String userKey = AdminAuthConstants.REFRESH_TOKEN_USER_KEY_PREFIX + userId;

        assertThat(redisTemplate.hasKey(tokenKey)).isFalse();
        assertThat(redisTemplate.hasKey(userKey)).isFalse();
    }
}
