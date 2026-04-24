package com.beta.core.notification;

import com.beta.core.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("디스코드 웹훅 실연동 확인이 필요할 때만 수동 실행")
class DiscordWebhookErrorLiveTest {

    @Test
    void 데이터베이스_연결_장애_발생시_503_디스코드_알림을_전송한다() {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler(createDiscordWebhookService());
        CannotGetJdbcConnectionException exception =
                new CannotGetJdbcConnectionException("Live test database unavailable");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/live-test/database-unavailable");

        // when
        var response = handler.handleDatabaseUnavailableException(exception, request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void 데이터베이스_타임아웃_발생시_503_디스코드_알림을_전송한다() {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler(createDiscordWebhookService());
        QueryTimeoutException exception = new QueryTimeoutException("Live test database timeout");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/live-test/database-timeout");

        // when
        var response = handler.handleDatabaseTimeoutException(exception, request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void 예상하지못한_예외_발생시_500_디스코드_알림을_전송한다() {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler(createDiscordWebhookService());
        RuntimeException exception = new RuntimeException("Live test unexpected exception");
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/live-test/unexpected-error");

        // when
        var response = handler.handleException(exception, request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    private DiscordWebhookService createDiscordWebhookService() {
        WebClient webClient = WebClient.builder().build();
        String webhookUrl = readEnvValue("DISCORD_ERROR_WEBHOOK_URL");
        assertThat(webhookUrl)
                .as("DISCORD_ERROR_WEBHOOK_URL must be set for live webhook test")
                .isNotBlank();
        return new DiscordWebhookService(webClient, webhookUrl, "beta-user-server");
    }

    private String readEnvValue(String key) {
        Path envPath = findEnvPath();

        try {
            List<String> lines = Files.readAllLines(envPath);
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                int separatorIndex = trimmedLine.indexOf('=');
                if (separatorIndex < 0) {
                    continue;
                }

                String currentKey = trimmedLine.substring(0, separatorIndex).trim();
                if (!currentKey.equals(key)) {
                    continue;
                }

                return trimmedLine.substring(separatorIndex + 1).trim();
            }
        } catch (IOException exception) {
            throw new IllegalStateException(".env file read failed", exception);
        }

        return null;
    }

    private Path findEnvPath() {
        Path currentPath = Path.of("").toAbsolutePath();

        for (int depth = 0; depth < 4 && currentPath != null; depth++) {
            Path envPath = currentPath.resolve(".env");
            if (Files.exists(envPath)) {
                return envPath;
            }
            currentPath = currentPath.getParent();
        }

        throw new IllegalStateException(".env file must exist for live webhook test");
    }
}
