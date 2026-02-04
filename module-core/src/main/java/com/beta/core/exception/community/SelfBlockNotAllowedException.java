package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class SelfBlockNotAllowedException extends BaseException {
    public SelfBlockNotAllowedException() {
        super(ErrorCode.SELF_BLOCK_NOT_ALLOWED);
    }
}
