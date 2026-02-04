package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class AlreadyBlockedException extends BaseException {
    public AlreadyBlockedException() {
        super(ErrorCode.ALREADY_BLOCKED);
    }
}
