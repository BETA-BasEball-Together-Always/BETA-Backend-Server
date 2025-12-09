package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

/**
 * 개인정보 수집 동의가 필요할 때 발생하는 예외
 */
public class PersonalInfoAgreementRequiredException extends BaseException {

    public PersonalInfoAgreementRequiredException() {
        super(ErrorCode.PERSONAL_INFO_AGREEMENT_REQUIRED);
    }

    public PersonalInfoAgreementRequiredException(String customMessage) {
        super(ErrorCode.PERSONAL_INFO_AGREEMENT_REQUIRED, customMessage);
    }
}
