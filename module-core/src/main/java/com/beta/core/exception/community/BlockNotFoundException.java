package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class BlockNotFoundException extends BaseException {
    public BlockNotFoundException() {
        super(ErrorCode.BLOCK_NOT_FOUND);
    }
}
