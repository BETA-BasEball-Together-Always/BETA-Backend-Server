package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * JWT 토큰이 유효하지 않을 때 발생하는 예외
 */
public class InvalidTokenException extends BaseException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(Throwable cause) {
        super(ErrorCode.INVALID_TOKEN, cause);
    }

    public InvalidTokenException(String customMessage) {
        super(ErrorCode.INVALID_TOKEN, customMessage);
    }
}
