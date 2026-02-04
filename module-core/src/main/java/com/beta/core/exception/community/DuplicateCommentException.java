package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class DuplicateCommentException extends BaseException {
    public DuplicateCommentException() {
        super(ErrorCode.DUPLICATE_COMMENT);
    }
}
