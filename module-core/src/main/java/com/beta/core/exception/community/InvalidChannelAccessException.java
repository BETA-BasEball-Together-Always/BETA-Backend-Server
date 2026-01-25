package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class InvalidChannelAccessException extends BaseException {
    public InvalidChannelAccessException(String message) {
        super(ErrorCode.INVALID_CHANNEL_ACCESS, message);
    }
}
