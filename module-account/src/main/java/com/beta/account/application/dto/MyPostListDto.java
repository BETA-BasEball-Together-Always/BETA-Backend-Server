package com.beta.account.application.dto;

import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.MyPostInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyPostListDto {

    private List<MyPostDto> posts;
    private boolean hasNext;
    private Long nextCursor;

    @Getter
    @Builder
    public static class MyPostDto {
        private Long postId;
        private AuthorInfo author;
        private String content;
        private String channel;
        private List<ImageInfo> images;
        private List<String> hashtags;
        private Integer likeCount;
        private Integer sadCount;
        private Integer funCount;
        private Integer hypeCount;
        private Integer commentCount;
        private LocalDateTime createdAt;

        public static MyPostDto from(MyPostInfo info) {
            List<ImageInfo> images = info.getImages().stream()
                    .map(img -> ImageInfo.builder()
                            .imageId(img.getImageId())
                            .imageUrl(img.getImageUrl())
                            .build())
                    .toList();

            return MyPostDto.builder()
                    .postId(info.getPostId())
                    .author(info.getAuthor())
                    .content(info.getContent())
                    .channel(info.getChannel())
                    .images(images)
                    .hashtags(info.getHashtags())
                    .likeCount(info.getLikeCount())
                    .sadCount(info.getSadCount())
                    .funCount(info.getFunCount())
                    .hypeCount(info.getHypeCount())
                    .commentCount(info.getCommentCount())
                    .createdAt(info.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ImageInfo {
        private Long imageId;
        private String imageUrl;
    }
}
