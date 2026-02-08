package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class SocialEmailNotProvidedException extends BaseException {

    public SocialEmailNotProvidedException() {
        super(ErrorCode.SOCIAL_EMAIL_NOT_PROVIDED);
    }

    public SocialEmailNotProvidedException(String customMessage) {
        super(ErrorCode.SOCIAL_EMAIL_NOT_PROVIDED, customMessage);
    }
}
