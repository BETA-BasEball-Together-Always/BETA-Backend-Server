package com.beta.docker;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class SearchSyncTestContainerIntegrationTest extends SearchSyncTestContainer {

    @Test
    void SearchSyncTestContainer를_상속한_클래스의_테스트를_실행하면_mysql과_elasticsearch와_logstash가_실행된다() {
        assertThat(mysql.isRunning()).isTrue();
        assertThat(elasticsearch.isRunning()).isTrue();
        assertThat(logstash.isRunning()).isTrue();
    }
}
