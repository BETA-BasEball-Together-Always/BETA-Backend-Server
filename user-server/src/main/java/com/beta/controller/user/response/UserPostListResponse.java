package com.beta.controller.user.response;

import com.beta.account.application.dto.UserProfilePostListDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "사용자 프로필 및 게시글 목록 응답")
public class UserPostListResponse {

    @Schema(description = "사용자 프로필 정보")
    private UserProfileInfo user;

    @Schema(description = "게시글 목록")
    private List<PostSummary> posts;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지 커서", example = "100")
    private Long nextCursor;

    public static UserPostListResponse from(UserProfilePostListDto dto) {
        UserProfileInfo userInfo = UserProfileInfo.builder()
                .userId(dto.getUser().getUserId())
                .nickname(dto.getUser().getNickname())
                .bio(dto.getUser().getBio())
                .teamCode(dto.getUser().getTeamCode())
                .teamName(dto.getUser().getTeamName())
                .build();

        List<PostSummary> posts = dto.getPosts().stream()
                .map(PostSummary::from)
                .toList();

        return UserPostListResponse.builder()
                .user(userInfo)
                .posts(posts)
                .hasNext(dto.isHasNext())
                .nextCursor(dto.getNextCursor())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "사용자 프로필 정보")
    public static class UserProfileInfo {

        @Schema(description = "사용자 ID", example = "123")
        private Long userId;

        @Schema(description = "닉네임", example = "야구팬")
        private String nickname;

        @Schema(description = "한줄소개", example = "두산 베어스 팬입니다")
        private String bio;

        @Schema(description = "응원 팀 코드", example = "DOOSAN")
        private String teamCode;

        @Schema(description = "응원 팀 이름", example = "두산 베어스")
        private String teamName;
    }

    @Getter
    @Builder
    @Schema(description = "게시글 요약 정보")
    public static class PostSummary {

        @Schema(description = "게시글 ID", example = "123")
        private Long postId;

        @Schema(description = "작성자 정보")
        private AuthorInfo author;

        @Schema(description = "게시글 내용", example = "오늘 경기 대박!")
        private String content;

        @Schema(description = "채널 (팀코드 또는 ALL)", example = "KIA")
        private String channel;

        @Schema(description = "이미지 목록")
        private List<ImageInfo> images;

        @Schema(description = "해시태그 목록", example = "[\"KIA\", \"야구\"]")
        private List<String> hashtags;

        @Schema(description = "감정표현 카운트")
        private EmotionCount emotions;

        @Schema(description = "댓글 수", example = "15")
        private Integer commentCount;

        @Schema(description = "작성일시", example = "2025-01-01T12:00:00")
        private LocalDateTime createdAt;

        public static PostSummary from(UserProfilePostListDto.PostDto dto) {
            List<ImageInfo> images = dto.getImages().stream()
                    .map(img -> ImageInfo.builder()
                            .imageId(img.getImageId())
                            .imageUrl(img.getImageUrl())
                            .build())
                    .toList();

            return PostSummary.builder()
                    .postId(dto.getPostId())
                    .author(AuthorInfo.from(dto.getAuthor()))
                    .content(dto.getContent())
                    .channel(dto.getChannel())
                    .images(images)
                    .hashtags(dto.getHashtags())
                    .emotions(EmotionCount.builder()
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
    @Schema(description = "이미지 정보")
    public static class ImageInfo {

        @Schema(description = "이미지 ID", example = "1")
        private Long imageId;

        @Schema(description = "이미지 URL", example = "https://objectstorage.ap-chuncheon-1.oraclecloud.com/...")
        private String imageUrl;
    }

    @Getter
    @Builder
    @Schema(description = "작성자 정보")
    public static class AuthorInfo {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "닉네임", example = "야구팬")
        private String nickname;

        @Schema(description = "응원 팀 코드", example = "KIA")
        private String teamCode;

        public static AuthorInfo from(com.beta.core.port.dto.AuthorInfo author) {
            return AuthorInfo.builder()
                    .userId(author.getUserId())
                    .nickname(author.getNickname())
                    .teamCode(author.getTeamCode())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "감정표현 카운트")
    public static class EmotionCount {

        @Schema(description = "좋아요 수", example = "10")
        private Integer likeCount;

        @Schema(description = "슬퍼요 수", example = "2")
        private Integer sadCount;

        @Schema(description = "웃겨요 수", example = "5")
        private Integer funCount;

        @Schema(description = "열광해요 수", example = "3")
        private Integer hypeCount;
    }
}
