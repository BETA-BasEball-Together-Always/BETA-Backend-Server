package com.beta.core.exception;

import com.beta.core.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 데이터베이스_연결_장애는_DATABASE_UNAVAILABLE_에러를_반환한다() {
        // given
        CannotGetJdbcConnectionException exception = new CannotGetJdbcConnectionException("Could not get JDBC Connection");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleDatabaseUnavailableException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE); // 503
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.DATABASE_UNAVAILABLE.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.DATABASE_UNAVAILABLE.getMessage());
    }

    @Test
    void 데이터베이스_타임아웃은_DATABASE_UNAVAILABLE_에러를_반환한다() {
        // given
        QueryTimeoutException exception = new QueryTimeoutException("Query timed out");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleDatabaseTimeoutException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE); // 503
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.DATABASE_UNAVAILABLE.getCode());
    }

    @Test
    void 알수없는_예외는_서버_내부_오류를_반환한다() {
        // given
        RuntimeException exception = new RuntimeException("unknown error");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    }
}
