package com.beta.core.exception.search;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class InvalidCursorException extends BaseException {
    public InvalidCursorException() {
        super(ErrorCode.INVALID_CURSOR);
    }
}
