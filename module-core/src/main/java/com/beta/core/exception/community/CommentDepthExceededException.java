package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class CommentDepthExceededException extends BaseException {
    public CommentDepthExceededException() {
        super(ErrorCode.COMMENT_DEPTH_EXCEEDED);
    }
}
