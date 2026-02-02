package com.beta.controller.account.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushSettingsResponse {
    private boolean success;
    private String message;

    public static PushSettingsResponse success(String message) {
        return PushSettingsResponse.builder()
                .success(true)
                .message(message)
                .build();
    }
}
