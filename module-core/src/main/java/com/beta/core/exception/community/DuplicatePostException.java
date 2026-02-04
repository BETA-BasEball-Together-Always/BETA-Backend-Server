package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class DuplicatePostException extends BaseException {
    public DuplicatePostException() {
        super(ErrorCode.DUPLICATE_POST);
    }
}
