package com.beta.controller.community.response;

import com.beta.community.application.dto.ImageInfo;
import com.beta.community.application.dto.PostListDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "게시글 목록 응답")
public class PostListResponse {

    @ArraySchema(arraySchema = @Schema(description = "게시글 목록"),
            schema = @Schema(implementation = PostSummary.class))
    private List<PostSummary> posts;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지 커서 (최신순 정렬 시 사용)", example = "100")
    private Long nextCursor;

    public static PostListResponse from(PostListDto dto) {
        List<PostSummary> posts = dto.getPosts().stream()
                .map(PostSummary::from)
                .toList();

        return PostListResponse.builder()
                .posts(posts)
                .hasNext(dto.isHasNext())
                .nextCursor(dto.getNextCursor())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "게시글 요약 정보")
    public static class PostSummary {

        @Schema(description = "게시글 ID", example = "1")
        private Long postId;

        @Schema(description = "게시글 내용", example = "오늘 경기 너무 좋았다!")
        private String content;

        @Schema(description = "채널 (ALL: 전체, 팀코드: 팀 채널)", example = "DOOSAN")
        private String channel;

        @ArraySchema(arraySchema = @Schema(description = "이미지 목록 (id, url 포함)"),
                schema = @Schema(implementation = ImageResponse.class))
        private List<ImageResponse> images;

        @Schema(description = "해시태그 목록", example = "[\"야구\", \"두산\"]")
        private List<String> hashtags;

        @Schema(description = "작성자 정보")
        private AuthorInfo author;

        @Schema(description = "감정표현 카운트")
        private EmotionCount emotions;

        @Schema(description = "댓글 수", example = "5")
        private Integer commentCount;

        @Schema(description = "작성일시", example = "2025-01-01T12:00:00")
        private LocalDateTime createdAt;

        public static PostSummary from(PostListDto.PostSummaryDto dto) {
            List<ImageResponse> images = dto.getImages().stream()
                    .map(ImageResponse::from)
                    .toList();

            return PostSummary.builder()
                    .postId(dto.getPostId())
                    .content(dto.getContent())
                    .channel(dto.getChannel())
                    .images(images)
                    .hashtags(dto.getHashtags())
                    .author(AuthorInfo.from(dto.getAuthor()))
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
    public static class ImageResponse {

        @Schema(description = "이미지 ID (삭제 요청 시 사용)", example = "1")
        private Long imageId;

        @Schema(description = "이미지 URL", example = "https://objectstorage.ap-chuncheon-1.oraclecloud.com/...")
        private String imageUrl;

        public static ImageResponse from(ImageInfo imageInfo) {
            return ImageResponse.builder()
                    .imageId(imageInfo.getImageId())
                    .imageUrl(imageInfo.getImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "작성자 정보")
    public static class AuthorInfo {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "닉네임", example = "야구팬123")
        private String nickname;

        @Schema(description = "응원 팀 코드", example = "DOOSAN")
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

        @Schema(description = "열광해요 수", example = "8")
        private Integer hypeCount;
    }
}
