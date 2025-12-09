package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 사용자 이름이 중복될 때 발생하는 예외
 */
public class NameDuplicateException extends BaseException {

    public NameDuplicateException() {
        super(ErrorCode.NAME_DUPLICATE);
    }

    public NameDuplicateException(String customMessage) {
        super(ErrorCode.NAME_DUPLICATE, customMessage);
    }
}
