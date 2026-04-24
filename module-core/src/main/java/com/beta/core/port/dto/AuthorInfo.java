package com.beta.core.port.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthorInfo {

    private final Long userId;
    private final String nickname;
    private final String teamCode;

    public static AuthorInfo unknown(Long userId) {
        return AuthorInfo.builder()
                .userId(userId)
                .nickname("알 수 없음")
                .teamCode(null)
                .build();
    }

    public static AuthorInfo withdrawn(Long userId) {
        return AuthorInfo.builder()
                .userId(userId)
                .nickname("탈퇴한 사용자")
                .teamCode(null)
                .build();
    }
}
