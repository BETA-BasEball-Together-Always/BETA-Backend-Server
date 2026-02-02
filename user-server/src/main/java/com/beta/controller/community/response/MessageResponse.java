package com.beta.controller.community.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageResponse {

    private String message;

    public static MessageResponse of(String message) {
        return MessageResponse.builder().message(message).build();
    }
}
