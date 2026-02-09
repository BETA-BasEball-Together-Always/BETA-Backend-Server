package com.beta.controller.community.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메시지 응답")
public class MessageResponse {

    @Schema(description = "응답 메시지", example = "처리가 완료되었습니다.")
    private String message;

    public static MessageResponse of(String message) {
        return MessageResponse.builder().message(message).build();
    }
}
