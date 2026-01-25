package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class InvalidImageException extends BaseException {
    public InvalidImageException(String message) {
        super(ErrorCode.INVALID_IMAGE, message);
    }
}
