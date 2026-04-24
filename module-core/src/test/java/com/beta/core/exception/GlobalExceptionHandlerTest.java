package com.beta.core.exception;

import com.beta.core.notification.DiscordWebhookService;
import com.beta.core.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class GlobalExceptionHandlerTest {

    private final DiscordWebhookService discordWebhookService = Mockito.mock(DiscordWebhookService.class);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(discordWebhookService);

    @Test
    void 데이터베이스_연결_장애는_DATABASE_UNAVAILABLE_에러를_반환한다() {
        // given
        CannotGetJdbcConnectionException exception = new CannotGetJdbcConnectionException("Could not get JDBC Connection");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleDatabaseUnavailableException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE); // 503
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.DATABASE_UNAVAILABLE.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.DATABASE_UNAVAILABLE.getMessage());
        verify(discordWebhookService)
                .sendErrorAlert("Database Unavailable", 503, "GET", "/api/test", exception);
    }

    @Test
    void 데이터베이스_타임아웃은_DATABASE_UNAVAILABLE_에러를_반환한다() {
        // given
        QueryTimeoutException exception = new QueryTimeoutException("Query timed out");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/posts");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleDatabaseTimeoutException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE); // 503
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.DATABASE_UNAVAILABLE.getCode());
        verify(discordWebhookService)
                .sendErrorAlert("Database Timeout", 503, "POST", "/api/posts", exception);
    }

    @Test
    void 알수없는_예외는_서버_내부_오류를_반환한다() {
        // given
        RuntimeException exception = new RuntimeException("unknown error");
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/posts/1");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        verify(discordWebhookService)
                .sendErrorAlert("Unexpected Server Error", 500, "DELETE", "/api/posts/1", exception);
    }
}
