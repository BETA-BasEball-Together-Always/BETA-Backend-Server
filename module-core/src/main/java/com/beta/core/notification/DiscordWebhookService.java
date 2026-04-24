package com.beta.core.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DiscordWebhookService {

    private static final long ALERT_DEDUP_WINDOW_MILLIS = 60_000L;

    private final WebClient webClient;
    private final String webhookUrl;
    private final String applicationName;
    private final Map<String, Long> alertSentAt = new ConcurrentHashMap<>();

    public DiscordWebhookService(
            WebClient webClient,
            @Value("${DISCORD_ERROR_WEBHOOK_URL:}") String webhookUrl,
            @Value("${management.metrics.tags.application:beta-server}") String applicationName
    ) {
        this.webClient = webClient;
        this.webhookUrl = webhookUrl;
        this.applicationName = applicationName;
    }

    @Async("discordAlertExecutor")
    public void sendErrorAlert(String title, int statusCode, String httpMethod, String requestUri, Exception exception) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        String dedupKey = createDedupKey(title, statusCode, httpMethod, requestUri, exception);
        if (!shouldSendAlert(dedupKey, System.currentTimeMillis())) {
            log.debug("Discord error alert suppressed: key={}", dedupKey);
            return;
        }

        String message = """
                ## [%d] %s
                서버: %s
                요청: %s %s
                예외: %s
                메시지: %s
                """.formatted(
                statusCode,
                title,
                applicationName,
                defaultValue(httpMethod),
                defaultValue(requestUri),
                exception.getClass().getSimpleName(),
                defaultValue(exception.getMessage())
        );

        try {
            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(Map.of("content", message))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception webhookException) {
            log.warn("Discord error alert send failed: {}", webhookException.getMessage(), webhookException);
        }
    }

    boolean shouldSendAlert(String dedupKey, long nowMillis) {
        Long previousSentAt = alertSentAt.putIfAbsent(dedupKey, nowMillis);
        if (previousSentAt == null) {
            return true;
        }

        if (nowMillis - previousSentAt < ALERT_DEDUP_WINDOW_MILLIS) {
            return false;
        }

        alertSentAt.put(dedupKey, nowMillis);
        return true;
    }

    String createDedupKey(String title, int statusCode, String httpMethod, String requestUri, Exception exception) {
        if (statusCode == 503) {
            return "503:" + title;
        }

        return "%d:%s:%s:%s:%s".formatted(
                statusCode,
                title,
                defaultValue(httpMethod),
                defaultValue(requestUri),
                exception.getClass().getSimpleName()
        );
    }

    String defaultValue(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
