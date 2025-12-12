package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class InvalidPasswordException extends BaseException {
  public InvalidPasswordException() {
    super(ErrorCode.INVALID_PASSWORD);
  }

  public InvalidPasswordException(String customMessage) {
    super(ErrorCode.INVALID_PASSWORD, customMessage);
  }
}
