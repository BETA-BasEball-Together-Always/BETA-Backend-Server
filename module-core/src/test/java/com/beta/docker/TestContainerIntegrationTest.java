package com.beta.docker;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestContainerIntegrationTest extends TestContainer {

    @Test
    void TestContainer를_상속한_클래스의_테스트를_실행하면_mysql과_redis가_실행된다() {
        assertThat(mysql.isRunning()).isTrue();
        assertThat(redis.isRunning()).isTrue();
    }
}
