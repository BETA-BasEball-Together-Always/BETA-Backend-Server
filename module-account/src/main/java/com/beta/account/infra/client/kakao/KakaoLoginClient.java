package com.beta.account.infra.client.kakao;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.infra.client.SocialLoginClient;
import com.beta.account.infra.client.SocialUserInfo;
import com.beta.core.exception.account.InvalidSocialTokenException;
import com.beta.core.exception.account.SocialApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLoginClient implements SocialLoginClient {

    private final WebClient webClient;

    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Override
    public SocialProvider supportedProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserInfoResponse response = webClient.get()
                    .uri(KAKAO_USERINFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserInfoResponse.class)
                    .timeout(TIMEOUT)
                    .blockOptional()
                    .orElseThrow(() -> new SocialApiException("카카오 API 응답이 비어있습니다"));

            if (response.getId() == null) {
                throw new SocialApiException("카카오 사용자 ID를 찾을 수 없습니다");
            }

            return SocialUserInfo.builder()
                    .socialId(String.valueOf(response.getId()))
                    .email(response.getEmail())
                    .build();

        } catch (WebClientResponseException.Unauthorized e) {
            log.warn("Invalid Kakao access token: {}", e.getMessage());
            throw new InvalidSocialTokenException("유효하지 않은 카카오 액세스 토큰입니다", e);

        } catch (WebClientResponseException e) {
            log.error("Kakao API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SocialApiException("카카오 API 호출 중 오류가 발생했습니다: " + e.getStatusCode(), e);

        } catch (Exception e) {
            log.error("Unexpected error while calling Kakao API: {}", e.getMessage(), e);
            throw new SocialApiException("카카오 사용자 정보 조회 중 오류가 발생했습니다", e);
        }
    }
}
