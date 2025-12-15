package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class PasswordCodeCooldownException extends BaseException {
  public PasswordCodeCooldownException() {
    super(ErrorCode.PASSWORD_CODE_COOLDOWN);
  }
  public PasswordCodeCooldownException(String customMessage) {
    super(ErrorCode.PASSWORD_CODE_COOLDOWN, customMessage);
  }
}
