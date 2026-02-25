package com.beta.core.exception.kbo;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class KboApiException extends BaseException {

    public KboApiException() {
        super(ErrorCode.KBO_API_ERROR);
    }

    public KboApiException(Throwable cause) {
        super(ErrorCode.KBO_API_ERROR, cause);
    }

    public KboApiException(String customMessage) {
        super(ErrorCode.KBO_API_ERROR, customMessage);
    }
}
