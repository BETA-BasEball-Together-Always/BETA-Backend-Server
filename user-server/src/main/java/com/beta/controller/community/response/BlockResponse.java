package com.beta.controller.community.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlockResponse {

    private Long userId;
    private Long blockedUserId;
    private boolean blocked;
}
