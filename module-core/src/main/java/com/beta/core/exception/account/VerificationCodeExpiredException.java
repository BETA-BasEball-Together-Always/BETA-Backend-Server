package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class VerificationCodeExpiredException extends BaseException {
    public VerificationCodeExpiredException() {
        super(ErrorCode.VERIFICATION_CODE_EXPIRED);
    }
    public VerificationCodeExpiredException(String customMessage) {
        super(ErrorCode.VERIFICATION_CODE_EXPIRED, customMessage);
    }
}
