package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 소셜 로그인 API 호출 중 오류가 발생했을 때 발생하는 예외
 */
public class SocialApiException extends BaseException {

    public SocialApiException() {
        super(ErrorCode.SOCIAL_API_ERROR);
    }

    public SocialApiException(Throwable cause) {
        super(ErrorCode.SOCIAL_API_ERROR, cause);
    }

    public SocialApiException(String customMessage) {
        super(ErrorCode.SOCIAL_API_ERROR, customMessage);
    }

    public SocialApiException(String customMessage, Throwable cause) {
        super(ErrorCode.SOCIAL_API_ERROR, customMessage, cause);
    }
}
