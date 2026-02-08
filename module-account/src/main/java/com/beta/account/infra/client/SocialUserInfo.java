package com.beta.account.infra.client;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialUserInfo {
    private String socialId;
    private String email;
}
