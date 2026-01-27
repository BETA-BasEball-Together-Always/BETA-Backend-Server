package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * Apple IdToken이 유효하지 않을 때 발생하는 예외
 */
public class InvalidAppleTokenException extends BaseException {

    public InvalidAppleTokenException() {
        super(ErrorCode.INVALID_APPLE_TOKEN);
    }

    public InvalidAppleTokenException(Throwable cause) {
        super(ErrorCode.INVALID_APPLE_TOKEN, cause);
    }

    public InvalidAppleTokenException(String customMessage) {
        super(ErrorCode.INVALID_APPLE_TOKEN, customMessage);
    }

    public InvalidAppleTokenException(String customMessage, Throwable cause) {
        super(ErrorCode.INVALID_APPLE_TOKEN, customMessage, cause);
    }
}
