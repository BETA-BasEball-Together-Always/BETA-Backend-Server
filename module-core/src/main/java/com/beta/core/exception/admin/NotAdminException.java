package com.beta.core.exception.admin;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class NotAdminException extends BaseException {

    public NotAdminException() {
        super(ErrorCode.ADMIN_NOT_ALLOWED);
    }
}
