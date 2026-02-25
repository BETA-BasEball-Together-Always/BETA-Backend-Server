package com.beta.controller.home.response;

import com.beta.community.application.dto.HomeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "홈 화면 응답")
public class HomeResponse {

    @Schema(description = "KBO 팀 순위 목록")
    private final List<TeamRankingResponse> teamRankings;

    @Schema(description = "인기 게시글 목록 (최근 24시간, 최대 5개)")
    private final List<PopularPostResponse> popularPosts;

    public static HomeResponse from(HomeDto dto) {
        return HomeResponse.builder()
                .teamRankings(dto.getTeamRankings().stream()
                        .map(TeamRankingResponse::from)
                        .toList())
                .popularPosts(dto.getPopularPosts().stream()
                        .map(PopularPostResponse::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "KBO 팀 순위")
    public static class TeamRankingResponse {
        @Schema(description = "순위", example = "1")
        private final int rank;

        @Schema(description = "팀 이름", example = "삼성")
        private final String teamName;

        @Schema(description = "경기 수", example = "144")
        private final int games;

        @Schema(description = "승", example = "87")
        private final int wins;

        @Schema(description = "패", example = "55")
        private final int losses;

        @Schema(description = "무", example = "2")
        private final int draws;

        @Schema(description = "승률", example = "0.613")
        private final String winRate;

        @Schema(description = "게임차", example = "-")
        private final String gamesBehind;

        @Schema(description = "최근 10경기", example = "7승3패")
        private final String recentTen;

        @Schema(description = "연속", example = "3연승")
        private final String streak;

        public static TeamRankingResponse from(HomeDto.TeamRankingDto dto) {
            return TeamRankingResponse.builder()
                    .rank(dto.getRank())
                    .teamName(dto.getTeamName())
                    .games(dto.getGames())
                    .wins(dto.getWins())
                    .losses(dto.getLosses())
                    .draws(dto.getDraws())
                    .winRate(dto.getWinRate())
                    .gamesBehind(dto.getGamesBehind())
                    .recentTen(dto.getRecentTen())
                    .streak(dto.getStreak())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "인기 게시글")
    public static class PopularPostResponse {
        @Schema(description = "게시글 ID", example = "1")
        private final Long postId;

        @Schema(description = "작성자 정보")
        private final AuthorResponse author;

        @Schema(description = "게시글 내용", example = "오늘 경기 대박!")
        private final String content;

        @Schema(description = "채널", example = "ALL")
        private final String channel;

        @Schema(description = "이미지 목록")
        private final List<ImageResponse> images;

        @Schema(description = "해시태그 목록")
        private final List<String> hashtags;

        @Schema(description = "감정표현 카운트")
        private final EmotionCountResponse emotions;

        @Schema(description = "댓글 수", example = "15")
        private final int commentCount;

        @Schema(description = "작성 시간")
        private final LocalDateTime createdAt;

        public static PopularPostResponse from(HomeDto.PopularPostDto dto) {
            return PopularPostResponse.builder()
                    .postId(dto.getPostId())
                    .author(AuthorResponse.builder()
                            .userId(dto.getAuthor().getUserId())
                            .nickname(dto.getAuthor().getNickname())
                            .teamCode(dto.getAuthor().getTeamCode())
                            .build())
                    .content(dto.getContent())
                    .channel(dto.getChannel())
                    .images(dto.getImages().stream()
                            .map(img -> ImageResponse.builder()
                                    .imageId(img.getImageId())
                                    .imageUrl(img.getImageUrl())
                                    .build())
                            .toList())
                    .hashtags(dto.getHashtags())
                    .emotions(EmotionCountResponse.builder()
                            .likeCount(dto.getLikeCount())
                            .sadCount(dto.getSadCount())
                            .funCount(dto.getFunCount())
                            .hypeCount(dto.getHypeCount())
                            .build())
                    .commentCount(dto.getCommentCount())
                    .createdAt(dto.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "작성자 정보")
    public static class AuthorResponse {
        @Schema(description = "사용자 ID", example = "1")
        private final Long userId;

        @Schema(description = "닉네임", example = "야구팬")
        private final String nickname;

        @Schema(description = "응원 팀 코드", example = "SAMSUNG")
        private final String teamCode;
    }

    @Getter
    @Builder
    @Schema(description = "이미지 정보")
    public static class ImageResponse {
        @Schema(description = "이미지 ID", example = "1")
        private final Long imageId;

        @Schema(description = "이미지 URL")
        private final String imageUrl;
    }

    @Getter
    @Builder
    @Schema(description = "감정표현 카운트")
    public static class EmotionCountResponse {
        @Schema(description = "좋아요 수", example = "10")
        private final int likeCount;

        @Schema(description = "슬퍼요 수", example = "2")
        private final int sadCount;

        @Schema(description = "웃겨요 수", example = "5")
        private final int funCount;

        @Schema(description = "신나요 수", example = "8")
        private final int hypeCount;
    }
}
