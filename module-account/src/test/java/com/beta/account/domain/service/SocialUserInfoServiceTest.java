package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.infra.client.SocialLoginClient;
import com.beta.account.infra.client.SocialLoginClientFactory;
import com.beta.account.infra.client.SocialUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialUserInfoService 단위 테스트")
class SocialUserInfoServiceTest {

    @Mock
    private SocialLoginClientFactory clientFactory;

    @InjectMocks
    private SocialUserInfoService socialUserInfoService;

    @Test
    @DisplayName("네이버 소셜 로그인으로 사용자 정보를 조회한다")
    void fetchSocialUserInfo_Success_WithNaver() {
        // given
        String accessToken = "naver-access-token";
        SocialProvider provider = SocialProvider.NAVER;
        String expectedSocialId = "naver-123456";

        SocialLoginClient mockClient = mock(SocialLoginClient.class);
        SocialUserInfo expectedUserInfo = SocialUserInfo.builder()
                .socialId(expectedSocialId)
                .build();

        when(clientFactory.getClient(provider)).thenReturn(mockClient);
        when(mockClient.getUserInfo(accessToken)).thenReturn(expectedUserInfo);

        // when
        SocialUserInfo actualUserInfo = socialUserInfoService.fetchSocialUserInfo(accessToken, provider);

        // then
        assertThat(actualUserInfo).isNotNull();
        assertThat(actualUserInfo.getSocialId()).isEqualTo(expectedSocialId);

        verify(clientFactory, times(1)).getClient(provider);
        verify(mockClient, times(1)).getUserInfo(accessToken);
    }

    @Test
    @DisplayName("카카오 소셜 로그인으로 사용자 정보를 조회한다")
    void fetchSocialUserInfo_Success_WithKakao() {
        // given
        String accessToken = "kakao-access-token";
        SocialProvider provider = SocialProvider.KAKAO;
        String expectedSocialId = "kakao-789012";

        SocialLoginClient mockClient = mock(SocialLoginClient.class);
        SocialUserInfo expectedUserInfo = SocialUserInfo.builder()
                .socialId(expectedSocialId)
                .build();

        when(clientFactory.getClient(provider)).thenReturn(mockClient);
        when(mockClient.getUserInfo(accessToken)).thenReturn(expectedUserInfo);

        // when
        SocialUserInfo actualUserInfo = socialUserInfoService.fetchSocialUserInfo(accessToken, provider);

        // then
        assertThat(actualUserInfo).isNotNull();
        assertThat(actualUserInfo.getSocialId()).isEqualTo(expectedSocialId);

        verify(clientFactory, times(1)).getClient(provider);
        verify(mockClient, times(1)).getUserInfo(accessToken);
    }

    @Test
    @DisplayName("Factory에서 적절한 클라이언트를 가져온다")
    void fetchSocialUserInfo_UsesCorrectClient() {
        // given
        String accessToken = "test-token";
        SocialProvider provider = SocialProvider.NAVER;

        SocialLoginClient mockClient = mock(SocialLoginClient.class);
        when(clientFactory.getClient(provider)).thenReturn(mockClient);
        when(mockClient.getUserInfo(accessToken)).thenReturn(
                SocialUserInfo.builder().socialId("test-id").build()
        );

        // when
        socialUserInfoService.fetchSocialUserInfo(accessToken, provider);

        // then
        verify(clientFactory, times(1)).getClient(provider);
    }
}
