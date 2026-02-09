package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;
import lombok.Getter;

@Getter
public class EmailDuplicateException extends BaseException {

    private final String existingSocialProvider;

    public EmailDuplicateException() {
        super(ErrorCode.EMAIL_DUPLICATE);
        this.existingSocialProvider = null;
    }

    public EmailDuplicateException(String customMessage) {
        super(ErrorCode.EMAIL_DUPLICATE, customMessage);
        this.existingSocialProvider = null;
    }

    public EmailDuplicateException(String customMessage, String existingSocialProvider) {
        super(ErrorCode.EMAIL_DUPLICATE, customMessage);
        this.existingSocialProvider = existingSocialProvider;
    }

    public static EmailDuplicateException withSocialProvider(String socialProvider) {
        String message = String.format("이미 %s로 가입된 이메일입니다.", socialProvider);
        return new EmailDuplicateException(message, socialProvider);
    }
}
