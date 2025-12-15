package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class SamePasswordException extends BaseException {
    public SamePasswordException() {
        super(ErrorCode.SAME_PASSWORD);
    }
    public SamePasswordException(String customMessage) {
        super(ErrorCode.SAME_PASSWORD, customMessage);
    }
}
