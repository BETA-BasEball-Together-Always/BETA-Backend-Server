package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.infra.client.SocialLoginClient;
import com.beta.account.infra.client.SocialLoginClientFactory;
import com.beta.account.infra.client.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialUserInfoService {

    private final SocialLoginClientFactory clientFactory;

    @Transactional(readOnly = true)
    public SocialUserInfo fetchSocialUserInfo(String token, SocialProvider socialProvider) {
        SocialLoginClient client = clientFactory.getClient(socialProvider);
        return client.getUserInfo(token);
    }
}
