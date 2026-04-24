package com.beta.core.notification;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordWebhookServiceTest {

    @Test
    void defaultValue는_blank값을_대시로_변환한다() {
        // given
        WebClient webClient = WebClient.builder().build();
        DiscordWebhookService service = new DiscordWebhookService(webClient, "", "beta-user-server");

        // when
        String nullValue = service.defaultValue(null);
        String blankValue = service.defaultValue(" ");
        String plainValue = service.defaultValue("POST");

        // then
        assertThat(nullValue).isEqualTo("-");
        assertThat(blankValue).isEqualTo("-");
        assertThat(plainValue).isEqualTo("POST");
    }

    @Test
    void shouldSendAlert는_같은_키를_1분_내에는_한번만_허용한다() {
        // given
        WebClient webClient = WebClient.builder().build();
        DiscordWebhookService service = new DiscordWebhookService(webClient, "", "beta-user-server");

        // when
        boolean first = service.shouldSendAlert("503:Database Unavailable", 1_000L);
        boolean second = service.shouldSendAlert("503:Database Unavailable", 30_000L);
        boolean third = service.shouldSendAlert("503:Database Unavailable", 61_001L);

        // then
        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(third).isTrue();
    }

    @Test
    void createDedupKey는_503이면_URI대신_에러종류기준으로_묶는다() {
        // given
        WebClient webClient = WebClient.builder().build();
        DiscordWebhookService service = new DiscordWebhookService(webClient, "", "beta-user-server");

        // when
        String dedupKey = service.createDedupKey(
                "Database Unavailable",
                503,
                "GET",
                "/api/v1/posts",
                new IllegalStateException("database down")
        );

        // then
        assertThat(dedupKey).isEqualTo("503:Database Unavailable");
    }

    @Test
    void createDedupKey는_500이면_요청과_예외기준으로_묶는다() {
        // given
        WebClient webClient = WebClient.builder().build();
        DiscordWebhookService service = new DiscordWebhookService(webClient, "", "beta-user-server");

        // when
        String dedupKey = service.createDedupKey(
                "Unexpected Server Error",
                500,
                "POST",
                "/api/v1/posts",
                new IllegalArgumentException("boom")
        );

        // then
        assertThat(dedupKey).isEqualTo("500:Unexpected Server Error:POST:/api/v1/posts:IllegalArgumentException");
    }
}
