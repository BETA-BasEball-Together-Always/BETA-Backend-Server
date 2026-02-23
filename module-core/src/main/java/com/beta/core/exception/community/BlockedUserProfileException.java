package com.beta.core.exception.community;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class BlockedUserProfileException extends BaseException {
    public BlockedUserProfileException() {
        super(ErrorCode.BLOCKED_USER_PROFILE);
    }
}
