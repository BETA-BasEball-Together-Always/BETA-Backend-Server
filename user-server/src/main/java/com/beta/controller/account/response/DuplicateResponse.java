package com.beta.controller.account.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DuplicateResponse {

    private boolean duplicate;

    public static DuplicateResponse of(boolean duplicate) {
        return DuplicateResponse.builder()
                .duplicate(duplicate)
                .build();
    }
}
