package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 닉네임 길이가 유효하지 않을 때 발생하는 예외 (2-13자)
 */
public class NicknameLengthException extends BaseException {

    public NicknameLengthException() {
        super(ErrorCode.INVALID_NICKNAME_LENGTH);
    }

    public NicknameLengthException(Throwable cause) {
        super(ErrorCode.INVALID_NICKNAME_LENGTH, cause);
    }

    public NicknameLengthException(String customMessage) {
        super(ErrorCode.INVALID_NICKNAME_LENGTH, customMessage);
    }

    public NicknameLengthException(String customMessage, Throwable cause) {
        super(ErrorCode.INVALID_NICKNAME_LENGTH, customMessage, cause);
    }
}
