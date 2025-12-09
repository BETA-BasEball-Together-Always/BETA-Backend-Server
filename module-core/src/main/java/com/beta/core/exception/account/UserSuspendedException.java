package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 정지된 사용자가 로그인 시도할 때 발생하는 예외
 */
public class UserSuspendedException extends BaseException {

    public UserSuspendedException() {
        super(ErrorCode.USER_SUSPENDED);
    }

    public UserSuspendedException(String customMessage) {
        super(ErrorCode.USER_SUSPENDED, customMessage);
    }
}
