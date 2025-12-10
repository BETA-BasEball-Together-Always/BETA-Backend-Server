package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;


public class EmailDuplicateException extends BaseException {

    public EmailDuplicateException() {
        super(ErrorCode.EMAIL_DUPLICATE);
    }

    public EmailDuplicateException(String customMessage) {
        super(ErrorCode.EMAIL_DUPLICATE, customMessage);
    }
}
