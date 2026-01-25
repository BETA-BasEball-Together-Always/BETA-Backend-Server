package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class ImageUploadException extends BaseException {
    public ImageUploadException(String message, Throwable cause) {
        super(ErrorCode.IMAGE_UPLOAD_FAILED, message, cause);
    }
}
