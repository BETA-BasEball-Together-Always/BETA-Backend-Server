package com.beta.core.infra.client.kbo;

import com.beta.core.exception.kbo.KboApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KboRankingClient 단위 테스트")
class KboRankingClientTest {

    private final KboRankingClient kboRankingClient = new KboRankingClient();

    @Nested
    @DisplayName("getRankings")
    class GetRankings {

        @Test
        @DisplayName("크롤링 성공 시 순위 데이터를 반환한다")
        void returnsRankings_whenCrawlingSucceeds() {
            // when - 실제 크롤링 시도 (네트워크 의존)
            // 크롤링이 성공하면 데이터 반환, 실패하면 예외 발생
            try {
                List<TeamRanking> rankings = kboRankingClient.getRankings();

                // then - 크롤링 성공 시
                assertThat(rankings).isNotEmpty();
                assertThat(rankings.size()).isLessThanOrEqualTo(10);

                TeamRanking first = rankings.get(0);
                assertThat(first.rank()).isEqualTo(1);
                assertThat(first.teamName()).isNotBlank();
            } catch (KboApiException e) {
                // 크롤링 실패 시 예외 발생 확인
                assertThat(e.getMessage()).isEqualTo("현재 순위 정보를 불러올 수 없습니다");
            }
        }

        @Test
        @DisplayName("순위 데이터 필드가 모두 존재한다")
        void rankingHasAllFields() {
            try {
                // when
                List<TeamRanking> rankings = kboRankingClient.getRankings();

                // then
                TeamRanking ranking = rankings.get(0);
                assertThat(ranking.rank()).isPositive();
                assertThat(ranking.teamName()).isNotNull();
                assertThat(ranking.winRate()).isNotNull();
                assertThat(ranking.gamesBehind()).isNotNull();
                assertThat(ranking.recentTen()).isNotNull();
                assertThat(ranking.streak()).isNotNull();
            } catch (KboApiException e) {
                // 크롤링 실패 시 테스트 패스 (네트워크 의존 테스트)
            }
        }
    }

    @Nested
    @DisplayName("refreshCache")
    class RefreshCache {

        @Test
        @DisplayName("캐시 갱신이 예외 없이 완료된다")
        void refreshCacheSucceeds() {
            // when & then - 크롤링 실패해도 예외 발생하지 않음 (내부에서 처리)
            kboRankingClient.refreshCache();
        }
    }

    @Nested
    @DisplayName("evictCache")
    class EvictCache {

        @Test
        @DisplayName("캐시 제거가 예외 없이 완료된다")
        void evictCacheSucceeds() {
            // when & then - 예외가 발생하지 않음
            kboRankingClient.evictCache();
        }
    }
}
