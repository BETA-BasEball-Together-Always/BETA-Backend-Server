package com.beta.community.application;

import com.beta.community.application.dto.HomeDto;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.service.UserBlockReadService;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.repository.PostQueryRepository;
import com.beta.core.infra.client.kbo.KboRankingClient;
import com.beta.core.infra.client.kbo.TeamRanking;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeAppService 단위 테스트")
class HomeAppServiceTest {

    @Mock
    private KboRankingClient kboRankingClient;

    @Mock
    private PostQueryRepository postQueryRepository;

    @Mock
    private PostImageJpaRepository postImageJpaRepository;

    @Mock
    private PostHashtagJpaRepository postHashtagJpaRepository;

    @Mock
    private UserBlockReadService userBlockReadService;

    @Mock
    private UserPort userPort;

    @InjectMocks
    private HomeAppService homeAppService;

    private static final Long USER_ID = 1L;
    private static final Long BLOCKED_USER_ID = 3L;

    @Nested
    @DisplayName("getHomeData")
    class GetHomeData {

        @Test
        @DisplayName("KBO 순위와 인기 게시글을 조합하여 반환한다")
        void returnsHomeData() {
            // given
            List<TeamRanking> rankings = List.of(
                    new TeamRanking(1, "삼성", 144, 87, 55, 2, "0.613", "-", "7승3패", "3연승"),
                    new TeamRanking(2, "LG", 144, 83, 59, 2, "0.585", "4.0", "6승4패", "1연승")
            );
            when(kboRankingClient.getRankings()).thenReturn(rankings);
            when(userBlockReadService.findBlockedUserIds(USER_ID)).thenReturn(List.of(BLOCKED_USER_ID));

            Post post = Post.builder()
                    .userId(USER_ID)
                    .content("테스트 게시글")
                    .channel("ALL")
                    .build();
            post.activate();
            when(postQueryRepository.findPopularPostsWithinHours(eq(24), eq(5), anyList()))
                    .thenReturn(List.of(post));

            when(postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(anyList(), any()))
                    .thenReturn(List.of());
            when(postHashtagJpaRepository.findByPost_IdIn(anyList()))
                    .thenReturn(List.of());
            when(userPort.findAuthorsByIds(anyList()))
                    .thenReturn(Map.of(USER_ID, AuthorInfo.builder()
                            .userId(USER_ID)
                            .nickname("테스트유저")
                            .teamCode("DOOSAN")
                            .build()));

            // when
            HomeDto result = homeAppService.getHomeData(USER_ID);

            // then
            assertThat(result.getTeamRankings()).hasSize(2);
            assertThat(result.getPopularPosts()).hasSize(1);
        }

        @Test
        @DisplayName("KBO 순위 정보가 정확히 변환된다")
        void convertsTeamRankingsCorrectly() {
            // given
            List<TeamRanking> rankings = List.of(
                    new TeamRanking(1, "삼성", 144, 87, 55, 2, "0.613", "-", "7승3패", "3연승")
            );
            when(kboRankingClient.getRankings()).thenReturn(rankings);
            when(userBlockReadService.findBlockedUserIds(USER_ID)).thenReturn(List.of());
            when(postQueryRepository.findPopularPostsWithinHours(eq(24), eq(5), anyList()))
                    .thenReturn(List.of());

            // when
            HomeDto result = homeAppService.getHomeData(USER_ID);

            // then
            assertThat(result.getTeamRankings()).hasSize(1);
            HomeDto.TeamRankingDto ranking = result.getTeamRankings().get(0);
            assertThat(ranking.getRank()).isEqualTo(1);
            assertThat(ranking.getTeamName()).isEqualTo("삼성");
            assertThat(ranking.getGames()).isEqualTo(144);
            assertThat(ranking.getWins()).isEqualTo(87);
            assertThat(ranking.getLosses()).isEqualTo(55);
            assertThat(ranking.getDraws()).isEqualTo(2);
            assertThat(ranking.getWinRate()).isEqualTo("0.613");
            assertThat(ranking.getGamesBehind()).isEqualTo("-");
            assertThat(ranking.getRecentTen()).isEqualTo("7승3패");
            assertThat(ranking.getStreak()).isEqualTo("3연승");
        }

        @Test
        @DisplayName("인기 게시글이 없으면 빈 리스트를 반환한다")
        void returnsEmptyPopularPostsWhenNone() {
            // given
            when(kboRankingClient.getRankings()).thenReturn(List.of());
            when(userBlockReadService.findBlockedUserIds(USER_ID)).thenReturn(List.of());
            when(postQueryRepository.findPopularPostsWithinHours(eq(24), eq(5), anyList()))
                    .thenReturn(List.of());
            // when
            HomeDto result = homeAppService.getHomeData(USER_ID);

            // then
            assertThat(result.getPopularPosts()).isEmpty();
        }

        @Test
        @DisplayName("차단한 사용자 ID가 쿼리에 전달된다")
        void passesBlockedUserIdsToQuery() {
            // given
            List<Long> blockedIds = List.of(BLOCKED_USER_ID, 4L, 5L);
            when(kboRankingClient.getRankings()).thenReturn(List.of());
            when(userBlockReadService.findBlockedUserIds(USER_ID)).thenReturn(blockedIds);
            when(postQueryRepository.findPopularPostsWithinHours(eq(24), eq(5), eq(blockedIds)))
                    .thenReturn(List.of());

            // when
            HomeDto result = homeAppService.getHomeData(USER_ID);

            // then
            assertThat(result.getPopularPosts()).isEmpty();
        }

        @Test
        @DisplayName("작성자 정보가 없으면 unknown 정보를 사용한다")
        void usesUnknownAuthorWhenNotFound() {
            // given
            when(kboRankingClient.getRankings()).thenReturn(List.of());
            when(userBlockReadService.findBlockedUserIds(USER_ID)).thenReturn(List.of());

            Post post = Post.builder()
                    .userId(999L)
                    .content("작성자 없는 게시글")
                    .channel("ALL")
                    .build();
            post.activate();
            when(postQueryRepository.findPopularPostsWithinHours(eq(24), eq(5), anyList()))
                    .thenReturn(List.of(post));

            when(postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(anyList(), any()))
                    .thenReturn(List.of());
            when(postHashtagJpaRepository.findByPost_IdIn(anyList()))
                    .thenReturn(List.of());
            when(userPort.findAuthorsByIds(anyList()))
                    .thenReturn(Map.of());

            // when
            HomeDto result = homeAppService.getHomeData(USER_ID);

            // then
            assertThat(result.getPopularPosts()).hasSize(1);
            assertThat(result.getPopularPosts().get(0).getAuthor().getUserId()).isEqualTo(999L);
            assertThat(result.getPopularPosts().get(0).getAuthor().getNickname()).isEqualTo("알 수 없음");
        }

        @Test
        @DisplayName("24시간 이내 인기 게시글이 5개 미만이면 전체 인기 게시글로 다시 조회한다")
        void fallsBackToOverallPopularPostsWhenRecentPostsAreLessThanLimit() {
            // given
            when(kboRankingClient.getRankings()).thenReturn(List.of());
            when(userBlockReadService.findBlockedUserIds(USER_ID)).thenReturn(List.of());

            Post recentPost = Post.builder()
                    .userId(USER_ID)
                    .content("최근 게시글")
                    .channel("ALL")
                    .build();
            recentPost.activate();

            Post olderPost = Post.builder()
                    .userId(USER_ID)
                    .content("전체 인기 게시글")
                    .channel("ALL")
                    .build();
            olderPost.activate();

            when(postQueryRepository.findPopularPostsWithinHours(eq(24), eq(5), anyList()))
                    .thenReturn(List.of(recentPost));
            when(postQueryRepository.findPopularPosts(anyList(), eq(5)))
                    .thenReturn(List.of(olderPost));
            when(postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(anyList(), any()))
                    .thenReturn(List.of());
            when(postHashtagJpaRepository.findByPost_IdIn(anyList()))
                    .thenReturn(List.of());
            when(userPort.findAuthorsByIds(anyList()))
                    .thenReturn(Map.of(USER_ID, AuthorInfo.builder()
                            .userId(USER_ID)
                            .nickname("테스트유저")
                            .teamCode("DOOSAN")
                            .build()));

            // when
            HomeDto result = homeAppService.getHomeData(USER_ID);

            // then
            assertThat(result.getPopularPosts()).hasSize(1);
            assertThat(result.getPopularPosts().get(0).getContent()).isEqualTo("전체 인기 게시글");
        }

        @Test
        @DisplayName("24시간 이내 인기 게시글이 5개면 전체 인기 게시글을 다시 조회하지 않는다")
        void doesNotFallbackWhenRecentPostsMeetLimit() {
            // given
            when(kboRankingClient.getRankings()).thenReturn(List.of());
            when(userBlockReadService.findBlockedUserIds(USER_ID)).thenReturn(List.of());

            Post firstPost = Post.builder()
                    .userId(USER_ID)
                    .content("게시글1")
                    .channel("ALL")
                    .build();
            firstPost.activate();

            Post secondPost = Post.builder()
                    .userId(USER_ID)
                    .content("게시글2")
                    .channel("ALL")
                    .build();
            secondPost.activate();

            Post thirdPost = Post.builder()
                    .userId(USER_ID)
                    .content("게시글3")
                    .channel("ALL")
                    .build();
            thirdPost.activate();

            Post fourthPost = Post.builder()
                    .userId(USER_ID)
                    .content("게시글4")
                    .channel("ALL")
                    .build();
            fourthPost.activate();

            Post fifthPost = Post.builder()
                    .userId(USER_ID)
                    .content("게시글5")
                    .channel("ALL")
                    .build();
            fifthPost.activate();

            List<Post> recentPosts = List.of(firstPost, secondPost, thirdPost, fourthPost, fifthPost);

            when(postQueryRepository.findPopularPostsWithinHours(eq(24), eq(5), anyList()))
                    .thenReturn(recentPosts);
            when(postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(anyList(), any()))
                    .thenReturn(List.of());
            when(postHashtagJpaRepository.findByPost_IdIn(anyList()))
                    .thenReturn(List.of());
            when(userPort.findAuthorsByIds(anyList()))
                    .thenReturn(Map.of(USER_ID, AuthorInfo.builder()
                            .userId(USER_ID)
                            .nickname("테스트유저")
                            .teamCode("DOOSAN")
                            .build()));

            // when
            HomeDto result = homeAppService.getHomeData(USER_ID);

            // then
            assertThat(result.getPopularPosts()).hasSize(5);
            verify(postQueryRepository, never()).findPopularPosts(anyList(), anyInt());
        }
    }
}
