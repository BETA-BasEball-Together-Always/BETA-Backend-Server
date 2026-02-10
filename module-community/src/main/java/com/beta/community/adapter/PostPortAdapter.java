package com.beta.community.adapter;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.PostHashtag;
import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.core.port.PostPort;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.PostInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostPortAdapter implements PostPort {

    private final PostJpaRepository postJpaRepository;
    private final PostImageJpaRepository postImageJpaRepository;
    private final PostHashtagJpaRepository postHashtagJpaRepository;
    private final UserPort userPort;

    @Override
    public Map<Long, PostInfo> findPostsByIds(List<Long> postIds, Long userId) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        List<Post> posts = postJpaRepository.findAllById(postIds);

        List<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .distinct()
                .toList();

        Map<Long, List<String>> imageUrlsMap = getImageUrlsMap(postIds);
        Map<Long, List<String>> hashtagsMap = getHashtagsMap(postIds);
        Map<Long, AuthorInfo> authorMap = userPort.findAuthorsByIds(userIds);

        return posts.stream()
                .collect(Collectors.toMap(
                        Post::getId,
                        post -> PostInfo.builder()
                                .id(post.getId())
                                .author(authorMap.getOrDefault(post.getUserId(), AuthorInfo.unknown(post.getUserId())))
                                .channel(post.getChannel().name())
                                .imageUrls(imageUrlsMap.getOrDefault(post.getId(), List.of()))
                                .hashtags(hashtagsMap.getOrDefault(post.getId(), List.of()))
                                .commentCount(post.getCommentCount())
                                .likeCount(post.getLikeCount())
                                .sadCount(post.getSadCount())
                                .funCount(post.getFunCount())
                                .hypeCount(post.getHypeCount())
                                .hasLiked(null) // TODO: 좋아요 여부 조회 추가해야함
                                .createdAt(post.getCreatedAt())
                                .build(),
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, List<String>> getImageUrlsMap(List<Long> postIds) {
        List<PostImage> images = postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(postIds, Status.ACTIVE);
        return images.stream()
                .collect(Collectors.groupingBy(
                        PostImage::getPostId,
                        Collectors.mapping(PostImage::getImgUrl, Collectors.toList())
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
