package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class InvalidVerificationCodeException extends BaseException {
    public InvalidVerificationCodeException() {
        super(ErrorCode.INVALID_VERIFICATION_CODE);
    }
    public InvalidVerificationCodeException(String customMessage) {
        super(ErrorCode.INVALID_VERIFICATION_CODE, customMessage);
    }
}
