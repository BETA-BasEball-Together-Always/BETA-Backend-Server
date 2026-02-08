package com.beta.community.application;

import com.beta.community.application.dto.CreatePostDto;
import com.beta.community.application.dto.PostDto;
import com.beta.community.application.dto.PostListDto;
import com.beta.community.application.dto.UpdatePostDto;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.PostHashtag;
import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import com.beta.community.domain.entity.UserBlock;
import com.beta.community.domain.service.*;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.repository.PostQueryRepository;
import com.beta.core.exception.community.AlreadyBlockedException;
import com.beta.core.exception.community.DuplicatePostException;
import com.beta.core.exception.community.InvalidImageException;
import com.beta.core.exception.community.PostAccessDeniedException;
import com.beta.core.exception.community.SelfBlockNotAllowedException;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityFacadeService {

    private final PostReadService postReadService;
    private final PostWriteService postWriteService;
    private final PostImageService postImageService;
    private final HashtagService hashtagService;
    private final ChannelValidationService channelValidationService;
    private final IdempotencyService idempotencyService;
    private final UserBlockReadService userBlockReadService;
    private final UserBlockWriteService userBlockWriteService;
    private final PostQueryRepository postQueryRepository;
    private final PostImageJpaRepository postImageJpaRepository;
    private final PostHashtagJpaRepository postHashtagJpaRepository;
    private final UserPort userPort;

    private static final int PAGE_SIZE = 10;

    @Transactional
    public PostDto createPost(Long userId, String userTeamCode, CreatePostDto dto) {

        if (idempotencyService.isDuplicatePost(userId, dto.getContent())) {
            throw new DuplicatePostException();
        }

        String targetChannel = channelValidationService.validateAndResolveChannel(
                dto.getChannel(),
                userTeamCode
        );

        postImageService.validateImages(dto.getImages());

        Post post = Post.builder()
                .userId(userId)
                .content(dto.getContent())
                .channel(targetChannel)
                .build();
        post.activate();
        Post savedPost = postWriteService.save(post);

        List<String> hashtags = hashtagService.processHashtags(
                savedPost,
                dto.getHashtags()
        );

        List<String> imageUrls = postImageService.uploadAndSaveImages(
                savedPost.getId(),
                userId,
                dto.getImages()
        );

        return PostDto.builder()
                .postId(savedPost.getId())
                .userId(savedPost.getUserId())
                .content(savedPost.getContent())
                .channel(savedPost.getChannel().name())
                .imageUrls(imageUrls)
                .hashtags(hashtags)
                .status(savedPost.getStatus().name())
                .createdAt(savedPost.getCreatedAt())
                .build();
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postReadService.findActiveById(postId);

        if (!post.isOwnedBy(userId)) {
            throw new PostAccessDeniedException();
        }

        List<String> hashtagNames = hashtagService.findHashtagNamesByPostId(postId);
        hashtagService.decrementUsageCounts(hashtagNames);
        postImageService.softDeleteAllByPostId(postId);
        postWriteService.softDelete(post);
    }

    @Transactional
    public PostDto updatePost(Long userId, Long postId, UpdatePostDto dto) {
        Post post = postReadService.findActiveById(postId);

        if (!post.isOwnedBy(userId)) {
            throw new PostAccessDeniedException();
        }

        List<String> currentImageUrls = postImageService.findActiveImageUrlsByPostId(postId);
        int deleteCount = dto.getDeletedImageIds() != null ? dto.getDeletedImageIds().size() : 0;
        int newCount = dto.getNewImages() != null ? dto.getNewImages().size() : 0;
        if (currentImageUrls.size() - deleteCount + newCount > 5) {
            throw new InvalidImageException(
                    String.format("이미지는 최대 5개까지 가능합니다 (현재 %d개, 삭제 %d개, 추가 %d개)",
                            currentImageUrls.size(), deleteCount, newCount)
            );
        }
        postImageService.validateImages(dto.getNewImages());

        // DB 조작
        List<String> deletedUrls = postImageService.softDeleteImages(dto.getDeletedImageIds(), userId);
        List<String> newImageUrls = postImageService.uploadAndSaveImages(postId, userId, dto.getNewImages());

        // 인메모리로 최종 이미지 URL 계산
        List<String> finalImageUrls = new ArrayList<>(currentImageUrls);
        finalImageUrls.removeAll(deletedUrls);
        finalImageUrls.addAll(newImageUrls);

        List<String> hashtags = hashtagService.updateHashtags(post, dto.getHashtags());
        postWriteService.updateContent(post, dto.getContent());

        return PostDto.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .content(dto.getContent())
                .channel(post.getChannel().name())
                .imageUrls(finalImageUrls)
                .hashtags(hashtags)
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .build();
    }

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new SelfBlockNotAllowedException();
        }
        if (userBlockReadService.isBlocked(blockerId, blockedId)) {
            throw new AlreadyBlockedException();
        }
        userBlockWriteService.save(UserBlock.builder()
                .blockerId(blockerId)
                .blockedId(blockedId)
                .build());
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        UserBlock userBlock = userBlockReadService.findByBlockerAndBlocked(blockerId, blockedId);
        userBlockWriteService.delete(userBlock);
    }

    @Transactional(readOnly = true)
    public PostListDto getPostList(Long userId, Long cursor, Integer offset, String channel, String sort) {
        List<Long> blockedUserIds = userBlockReadService.findBlockedUserIds(userId);

        boolean isPopular = "popular".equalsIgnoreCase(sort);
        List<Post> posts;

        if (isPopular) {
            int effectiveOffset = (offset != null) ? offset : 0;
            posts = postQueryRepository.findPostsWithOffset(effectiveOffset, channel, blockedUserIds);
        } else {
            posts = postQueryRepository.findPostsWithCursor(cursor, channel, blockedUserIds);
        }

        boolean hasNext = posts.size() > PAGE_SIZE;
        if (hasNext) {
            posts = posts.subList(0, PAGE_SIZE);
        }

        Long nextCursor = (!isPopular && hasNext && !posts.isEmpty()) ? posts.getLast().getId() : null;

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();

        Map<Long, List<String>> imageUrlsMap = getImageUrlsMap(postIds);
        Map<Long, List<String>> hashtagsMap = getHashtagsMap(postIds);
        Map<Long, AuthorInfo> authorMap = userPort.findAuthorsByIds(userIds);

        List<PostListDto.PostSummaryDto> postSummaries = posts.stream()
                .map(post -> PostListDto.PostSummaryDto.builder()
                        .postId(post.getId())
                        .author(authorMap.getOrDefault(post.getUserId(), AuthorInfo.unknown(post.getUserId())))
                        .content(post.getContent())
                        .channel(post.getChannel().name())
                        .imageUrls(imageUrlsMap.getOrDefault(post.getId(), List.of()))
                        .hashtags(hashtagsMap.getOrDefault(post.getId(), List.of()))
                        .likeCount(post.getLikeCount())
                        .sadCount(post.getSadCount())
                        .funCount(post.getFunCount())
                        .hypeCount(post.getHypeCount())
                        .commentCount(post.getCommentCount())
                        .createdAt(post.getCreatedAt())
                        .build())
                .toList();

        return PostListDto.builder()
                .posts(postSummaries)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    private Map<Long, List<String>> getImageUrlsMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        List<PostImage> images = postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(postIds, Status.ACTIVE);
        return images.stream()
                .collect(Collectors.groupingBy(
                        PostImage::getPostId,
                        Collectors.mapping(PostImage::getImgUrl, Collectors.toList())
                ));
    }

    private Map<Long, List<String>> getHashtagsMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        List<PostHashtag> postHashtags = postHashtagJpaRepository.findByPost_IdIn(postIds);
        return postHashtags.stream()
                .collect(Collectors.groupingBy(
                        ph -> ph.getPost().getId(),
                        Collectors.mapping(ph -> ph.getHashtag().getTagName(), Collectors.toList())
                ));
    }
}
