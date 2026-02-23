package com.beta.account.application.dto;

import com.beta.account.domain.entity.User;
import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.MyPostInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserProfilePostListDto {

    private UserProfileInfo user;
    private List<PostDto> posts;
    private boolean hasNext;
    private Long nextCursor;

    public static UserProfilePostListDto of(User targetUser, List<PostDto> posts, boolean hasNext, Long nextCursor) {
        return UserProfilePostListDto.builder()
                .user(UserProfileInfo.from(targetUser))
                .posts(posts)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    @Getter
    @Builder
    public static class UserProfileInfo {
        private Long userId;
        private String nickname;
        private String bio;
        private String teamCode;
        private String teamName;

        public static UserProfileInfo from(User user) {
            return UserProfileInfo.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .bio(user.getBio())
                    .teamCode(user.getBaseballTeam() != null ? user.getBaseballTeam().getCode() : null)
                    .teamName(user.getBaseballTeam() != null ? user.getBaseballTeam().getTeamNameKr() : null)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PostDto {
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

        public static PostDto from(MyPostInfo info) {
            List<ImageInfo> images = info.getImages().stream()
                    .map(img -> ImageInfo.builder()
                            .imageId(img.getImageId())
                            .imageUrl(img.getImageUrl())
                            .build())
                    .toList();

            return PostDto.builder()
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
