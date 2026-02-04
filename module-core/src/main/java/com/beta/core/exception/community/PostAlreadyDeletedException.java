package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class PostAlreadyDeletedException extends BaseException {
    public PostAlreadyDeletedException() {
        super(ErrorCode.POST_ALREADY_DELETED);
    }
}
