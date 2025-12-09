package com.beta.account.infra.client.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
class KakaoUserInfoResponse {

    @JsonProperty("id")
    private Long id;
}
