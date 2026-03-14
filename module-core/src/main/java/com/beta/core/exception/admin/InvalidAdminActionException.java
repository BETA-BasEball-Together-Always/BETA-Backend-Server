package com.beta.core.exception.admin;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class InvalidAdminActionException extends BaseException {

    public InvalidAdminActionException(String customMessage) {
        super(ErrorCode.INVALID_ADMIN_ACTION, customMessage);
    }

}
