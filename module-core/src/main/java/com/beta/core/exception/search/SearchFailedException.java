package com.beta.core.exception.search;

import com.beta.core.exception.BaseException;
import com.beta.core.exception.ErrorCode;

public class SearchFailedException extends BaseException {
    public SearchFailedException() {
        super(ErrorCode.SEARCH_FAILED);
    }
}
