package com.beta.community.adapter;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.PostHashtag;
import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.CommentJpaRepository;
import com.beta.community.infra.repository.EmotionJpaRepository;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.repository.PostQueryRepository;
import com.beta.core.port.CommunityPort;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.MyPostInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommunityPortAdapter implements CommunityPort {

    private final PostQueryRepository postQueryRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final EmotionJpaRepository emotionJpaRepository;
    private final PostImageJpaRepository postImageJpaRepository;
    private final PostHashtagJpaRepository postHashtagJpaRepository;
    private final UserPort userPort;

    @Override
    public List<MyPostInfo> findMyPosts(Long userId, Long cursor, int pageSize) {
        List<Post> posts = postQueryRepository.findPostsByUserId(userId, cursor, pageSize);
        return toMyPostInfoList(posts);
    }

    @Override
    public List<MyPostInfo> findCommentedPosts(Long userId, Long cursor, int pageSize) {
        List<Long> postIds = commentJpaRepository.findDistinctPostIdsByUserId(userId);
        List<Post> posts = postQueryRepository.findPostsByIdsWithCursor(postIds, cursor, pageSize);
        return toMyPostInfoList(posts);
    }

    @Override
    public List<MyPostInfo> findLikedPosts(Long userId, Long cursor, int pageSize) {
        List<Long> postIds = emotionJpaRepository.findPostIdsByUserId(userId);
        List<Post> posts = postQueryRepository.findPostsByIdsWithCursor(postIds, cursor, pageSize);
        return toMyPostInfoList(posts);
    }

    @Override
    public List<MyPostInfo> findUserPostsWithChannelFilter(Long userId, List<String> channels, Long cursor, int pageSize) {
        List<Post> posts = postQueryRepository.findPostsByUserIdWithChannelFilter(userId, channels, cursor, pageSize);
        return toMyPostInfoList(posts);
    }

    private List<MyPostInfo> toMyPostInfoList(List<Post> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();

        Map<Long, List<MyPostInfo.ImageInfo>> imagesMap = getImagesMap(postIds);
        Map<Long, List<String>> hashtagsMap = getHashtagsMap(postIds);
        Map<Long, AuthorInfo> authorMap = userPort.findAuthorsByIds(userIds);

        return posts.stream()
                .map(post -> MyPostInfo.builder()
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
                        .build())
                .toList();
    }

    private Map<Long, List<MyPostInfo.ImageInfo>> getImagesMap(List<Long> postIds) {
        List<PostImage> images = postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(postIds, Status.ACTIVE);
        return images.stream()
                .collect(Collectors.groupingBy(
                        PostImage::getPostId,
                        Collectors.mapping(
                                img -> MyPostInfo.ImageInfo.builder()
                                        .imageId(img.getId())
                                        .imageUrl(img.getImgUrl())
                                        .build(),
                                Collectors.toList()
                        )
                ));
    }

    private Map<Long, List<String>> getHashtagsMap(List<Long> postIds) {
        List<PostHashtag> postHashtags = postHashtagJpaRepository.findByPost_IdIn(postIds);
        return postHashtags.stream()
                .collect(Collectors.groupingBy(
                        ph -> ph.getPost().getId(),
                        Collectors.mapping(ph -> ph.getHashtag().getTagName(), Collectors.toList())
                ));
    }
}
