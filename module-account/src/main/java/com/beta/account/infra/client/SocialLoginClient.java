package com.beta.account.infra.client;


import com.beta.account.application.dto.SocialProvider;

public interface SocialLoginClient {

    SocialProvider supportedProvider();

    SocialUserInfo getUserInfo(String accessToken);
}
