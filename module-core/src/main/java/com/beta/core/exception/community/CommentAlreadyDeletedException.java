package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class CommentAlreadyDeletedException extends BaseException {
    public CommentAlreadyDeletedException() {
        super(ErrorCode.COMMENT_ALREADY_DELETED);
    }
}
