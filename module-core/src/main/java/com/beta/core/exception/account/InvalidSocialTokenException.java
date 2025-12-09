package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 소셜 로그인 토큰이 유효하지 않을 때 발생하는 예외
 */
public class InvalidSocialTokenException extends BaseException {

    public InvalidSocialTokenException() {
        super(ErrorCode.INVALID_SOCIAL_TOKEN);
    }

    public InvalidSocialTokenException(Throwable cause) {
        super(ErrorCode.INVALID_SOCIAL_TOKEN, cause);
    }

    public InvalidSocialTokenException(String customMessage) {
        super(ErrorCode.INVALID_SOCIAL_TOKEN, customMessage);
    }

    public InvalidSocialTokenException(String customMessage, Throwable cause) {
        super(ErrorCode.INVALID_SOCIAL_TOKEN, customMessage, cause);
    }
}
