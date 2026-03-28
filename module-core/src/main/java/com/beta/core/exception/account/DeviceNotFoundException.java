package com.beta.core.exception.account;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class DeviceNotFoundException extends BaseException {

    public DeviceNotFoundException() {
        super(ErrorCode.DEVICE_NOT_FOUND);
    }

    public DeviceNotFoundException(String customMessage) {
        super(ErrorCode.DEVICE_NOT_FOUND, customMessage);
    }
}
