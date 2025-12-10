package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 팀을 찾을 수 없을 때 발생하는 예외
 */
public class TeamNotFoundException extends BaseException {

    public TeamNotFoundException() {
        super(ErrorCode.TEAM_NOT_FOUND);
    }

    public TeamNotFoundException(String customMessage) {
        super(ErrorCode.TEAM_NOT_FOUND, customMessage);
    }
}
