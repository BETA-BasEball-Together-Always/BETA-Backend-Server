package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 회원가입 단계가 올바르지 않을 때 발생하는 예외
 */
public class InvalidSignupStepException extends BaseException {

    public InvalidSignupStepException() {
        super(ErrorCode.INVALID_SIGNUP_STEP);
    }

    public InvalidSignupStepException(Throwable cause) {
        super(ErrorCode.INVALID_SIGNUP_STEP, cause);
    }

    public InvalidSignupStepException(String customMessage) {
        super(ErrorCode.INVALID_SIGNUP_STEP, customMessage);
    }

    public InvalidSignupStepException(String customMessage, Throwable cause) {
        super(ErrorCode.INVALID_SIGNUP_STEP, customMessage, cause);
    }
}
