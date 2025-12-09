package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 탈퇴한 사용자가 로그인 시도할 때 발생하는 예외
 */
public class UserWithdrawnException extends BaseException {

    public UserWithdrawnException() {
        super(ErrorCode.USER_WITHDRAWN);
    }

    public UserWithdrawnException(String customMessage) {
        super(ErrorCode.USER_WITHDRAWN, customMessage);
    }
}
