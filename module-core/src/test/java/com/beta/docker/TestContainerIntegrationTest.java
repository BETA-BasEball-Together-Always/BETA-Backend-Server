package com.beta.docker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestContainerIntegrationTest extends TestContainer {

    @Test
    void TestContainer를_상속한_클래스의_테스트를_실행하면_정의된_모든_컨테이너가_실행된다() {
        assertThat(mysql.isRunning()).isTrue();
        assertThat(redis.isRunning()).isTrue();
        assertThat(elasticsearch.isRunning()).isTrue();
    }
}
