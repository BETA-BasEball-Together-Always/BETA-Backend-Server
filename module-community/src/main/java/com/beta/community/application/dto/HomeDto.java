package com.beta.community.application.dto;

import com.beta.community.domain.entity.Post;
import com.beta.core.infra.client.kbo.TeamRanking;
import com.beta.core.port.dto.AuthorInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class HomeDto {
    private final List<TeamRankingDto> teamRankings;
    private final List<PopularPostDto> popularPosts;

    @Getter
    @Builder
    public static class TeamRankingDto {
        private final int rank;
        private final String teamName;
        private final int games;
        private final int wins;
        private final int losses;
        private final int draws;
        private final String winRate;
        private final String gamesBehind;
        private final String recentTen;
        private final String streak;

        public static TeamRankingDto from(TeamRanking ranking) {
            return TeamRankingDto.builder()
                    .rank(ranking.rank())
                    .teamName(ranking.teamName())
                    .games(ranking.games())
                    .wins(ranking.wins())
                    .losses(ranking.losses())
                    .draws(ranking.draws())
                    .winRate(ranking.winRate())
                    .gamesBehind(ranking.gamesBehind())
                    .recentTen(ranking.recentTen())
                    .streak(ranking.streak())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PopularPostDto {
        private final Long postId;
        private final AuthorInfo author;
        private final String content;
        private final String channel;
        private final List<ImageInfo> images;
        private final List<String> hashtags;
        private final int likeCount;
        private final int sadCount;
        private final int funCount;
        private final int hypeCount;
        private final int commentCount;
        private final LocalDateTime createdAt;

        public static PopularPostDto from(
                Post post,
                Map<Long, List<ImageInfo>> imagesMap,
                Map<Long, List<String>> hashtagsMap,
                Map<Long, AuthorInfo> authorMap
        ) {
            return PopularPostDto.builder()
                    .postId(post.getId())
                    .author(authorMap.getOrDefault(post.getUserId(), AuthorInfo.unknown(post.getUserId())))
                    .content(post.getContent())
                    .channel(post.getChannel().name())
                    .images(imagesMap.getOrDefault(post.getId(), List.of()))
                    .hashtags(hashtagsMap.getOrDefault(post.getId(), List.of()))
                    .likeCount(post.getLikeCount())
                    .sadCount(post.getSadCount())
                    .funCount(post.getFunCount())
                    .hypeCount(post.getHypeCount())
                    .commentCount(post.getCommentCount())
                    .createdAt(post.getCreatedAt())
                    .build();
        }
    }
}
