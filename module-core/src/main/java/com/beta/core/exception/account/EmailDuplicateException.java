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
        String providerName = switch (socialProvider) {
            case "NAVER" -> "네이버";
            case "KAKAO" -> "카카오";
            case "APPLE" -> "애플";
            default -> socialProvider;
        };
        String message = String.format("%s로 가입된 계정입니다. %s 로그인을 이용해주세요.", providerName, providerName);
        return new EmailDuplicateException(message, socialProvider);
    }
}
