package com.beta.account.infra.client.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
class NaverUserInfoResponse {

    @JsonProperty("response")
    private ResponseData response;

    @Getter
    @NoArgsConstructor
    static class ResponseData {
        private String id;
        private String email;
    }

    public String getSocialId() {
        return response != null ? response.getId() : null;
    }

    public String getEmail() {
        return response != null ? response.getEmail() : null;
    }
}
