package com.beta.account.infra.client.naver;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.infra.client.SocialUserInfo;
import com.beta.core.exception.ErrorCode;
import com.beta.core.exception.account.InvalidSocialTokenException;
import com.beta.core.exception.account.SocialApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NaverLoginClient 단위 테스트")
class NaverLoginClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private NaverLoginClient naverLoginClient;

    @BeforeEach
    void setUp() {
        naverLoginClient = new NaverLoginClient(webClient);
    }

    @Test
    @DisplayName("지원하는 소셜 프로바이더는 NAVER이다")
    void supportedProvider_ReturnsNaver() {
        // when
        SocialProvider provider = naverLoginClient.supportedProvider();

        // then
        assertThat(provider).isEqualTo(SocialProvider.NAVER);
    }

    @Test
    @DisplayName("유효한 액세스 토큰으로 사용자 정보를 조회한다")
    void getUserInfo_Success_WithValidToken() {
        // given
        String accessToken = "valid-naver-token";
        String expectedSocialId = "naver-123456";

        NaverUserInfoResponse mockResponse = mock(NaverUserInfoResponse.class);
        when(mockResponse.getSocialId()).thenReturn(expectedSocialId);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverUserInfoResponse.class))
                .thenReturn(Mono.just(mockResponse));

        // when
        SocialUserInfo userInfo = naverLoginClient.getUserInfo(accessToken);

        // then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getSocialId()).isEqualTo(expectedSocialId);
    }

    @Test
    @DisplayName("유효하지 않은 액세스 토큰으로 조회 시 InvalidSocialTokenException을 발생시킨다")
    void getUserInfo_ThrowsInvalidSocialTokenException_WithInvalidToken() {
        // given
        String invalidToken = "invalid-naver-token";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverUserInfoResponse.class))
                .thenReturn(Mono.<NaverUserInfoResponse>error(
                        WebClientResponseException.create(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                null,
                                null,
                                null
                        )
                ));

        // when & then
        assertThatThrownBy(() -> naverLoginClient.getUserInfo(invalidToken))
                .isInstanceOf(InvalidSocialTokenException.class)
                .hasMessageContaining("유효하지 않은 네이버 액세스 토큰입니다")
                .satisfies(exception -> {
                    InvalidSocialTokenException ex = (InvalidSocialTokenException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_SOCIAL_TOKEN);
                });
    }

    @Test
    @DisplayName("네이버 API 오류 발생 시 SocialApiException을 발생시킨다")
    void getUserInfo_ThrowsSocialApiException_WhenApiError() {
        // given
        String accessToken = "valid-token";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverUserInfoResponse.class))
                .thenReturn(Mono.<NaverUserInfoResponse>error(
                        WebClientResponseException.create(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                null,
                                null,
                                null
                        )
                ));

        // when & then
        assertThatThrownBy(() -> naverLoginClient.getUserInfo(accessToken))
                .isInstanceOf(SocialApiException.class)
                .hasMessageContaining("네이버 API 호출 중 오류가 발생했습니다")
                .satisfies(exception -> {
                    SocialApiException ex = (SocialApiException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_API_ERROR);
                });
    }

    @Test
    @DisplayName("네이버 API 응답이 비어있을 때 SocialApiException을 발생시킨다")
    void getUserInfo_ThrowsSocialApiException_WhenResponseIsEmpty() {
        // given
        String accessToken = "valid-token";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverUserInfoResponse.class))
                .thenReturn(Mono.<NaverUserInfoResponse>error(new java.util.NoSuchElementException()));

        // when & then
        assertThatThrownBy(() -> naverLoginClient.getUserInfo(accessToken))
                .isInstanceOf(SocialApiException.class)
                .hasMessageContaining("네이버 사용자 정보 조회 중 오류가 발생했습니다")
                .satisfies(exception -> {
                    SocialApiException ex = (SocialApiException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_API_ERROR);
                });
    }

    @Test
    @DisplayName("응답에서 사용자 ID를 찾을 수 없을 때 SocialApiException을 발생시킨다")
    void getUserInfo_ThrowsSocialApiException_WhenSocialIdIsNull() {
        // given
        String accessToken = "valid-token";

        NaverUserInfoResponse mockResponse = mock(NaverUserInfoResponse.class);
        when(mockResponse.getSocialId()).thenReturn(null);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverUserInfoResponse.class))
                .thenReturn(Mono.just(mockResponse));

        // when & then
        assertThatThrownBy(() -> naverLoginClient.getUserInfo(accessToken))
                .isInstanceOf(SocialApiException.class)
                .satisfies(exception -> {
                    SocialApiException ex = (SocialApiException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_API_ERROR);
                    assertThat(ex.getMessage()).isIn(
                            "네이버 사용자 ID를 찾을 수 없습니다",
                            "네이버 사용자 정보 조회 중 오류가 발생했습니다"
                    );
                });
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 SocialApiException을 발생시킨다")
    void getUserInfo_ThrowsSocialApiException_WhenUnexpectedError() {
        // given
        String accessToken = "valid-token";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverUserInfoResponse.class))
                .thenReturn(Mono.<NaverUserInfoResponse>error(new RuntimeException("Unexpected error")));

        // when & then
        assertThatThrownBy(() -> naverLoginClient.getUserInfo(accessToken))
                .isInstanceOf(SocialApiException.class)
                .hasMessageContaining("네이버 사용자 정보 조회 중 오류가 발생했습니다")
                .satisfies(exception -> {
                    SocialApiException ex = (SocialApiException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_API_ERROR);
                });
    }
}
