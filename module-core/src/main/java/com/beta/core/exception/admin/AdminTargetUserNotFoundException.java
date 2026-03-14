package com.beta.core.exception.admin;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class AdminTargetUserNotFoundException extends BaseException {

    public AdminTargetUserNotFoundException() {
        super(ErrorCode.ADMIN_TARGET_USER_NOT_FOUND);
    }

}
