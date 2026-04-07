package com.beta.community.application.admin;

import com.beta.community.application.admin.dto.AdminPostCommentsResult;
import com.beta.community.application.admin.dto.AdminPostDetailResult;
import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.PostHashtag;
import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import com.beta.community.domain.service.CommentReadService;
import com.beta.community.domain.service.PostReadService;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminPostDetailFacadeService {

    private static final int COMMENT_PAGE_SIZE = 20;

    private final PostReadService postReadService;
    private final CommentReadService commentReadService;
    private final PostImageJpaRepository postImageJpaRepository;
    private final PostHashtagJpaRepository postHashtagJpaRepository;
    private final UserPort userPort;

    @Transactional(readOnly = true)
    public AdminPostDetailResult getPostDetail(Long postId) {
        Post post = postReadService.findById(postId);

        List<AdminPostDetailResult.ImageResult> images = getImagesMap(List.of(postId)).getOrDefault(postId, List.of());
        List<String> hashtags = getHashtagsMap(List.of(postId)).getOrDefault(postId, List.of());

        AdminCommentPage commentPage = fetchCommentsWithTree(postId, null);

        List<Long> allUserIds = Stream.concat(
                        Stream.of(post.getUserId()),
                        commentPage.allComments().stream().map(Comment::getUserId)
                )
                .distinct()
                .toList();
        Map<Long, AuthorInfo> authorMap = userPort.findAuthorsByIds(allUserIds);

        List<AdminPostDetailResult.CommentResult> comments = buildCommentTree(
                commentPage.parentComments(),
                commentPage.replies(),
                authorMap
        );

        AuthorInfo postAuthor = authorMap.getOrDefault(post.getUserId(), AuthorInfo.unknown(post.getUserId()));

        return new AdminPostDetailResult(
                post.getId(),
                new AdminPostDetailResult.AuthorResult(
                        postAuthor.getUserId(),
                        postAuthor.getNickname(),
                        postAuthor.getTeamCode()
                ),
                post.getContent(),
                post.getChannel(),
                post.getStatus(),
                images,
                hashtags,
                new AdminPostDetailResult.EmotionResult(
                        post.getLikeCount(),
                        post.getSadCount(),
                        post.getFunCount(),
                        post.getHypeCount()
                ),
                post.getCommentCount(),
                post.getCreatedAt(),
                comments,
                commentPage.hasNext(),
                commentPage.nextCursor()
        );
    }

    @Transactional(readOnly = true)
    public AdminPostCommentsResult getComments(Long postId, Long cursor) {
        postReadService.findById(postId);

        AdminCommentPage commentPage = fetchCommentsWithTree(postId, cursor);

        List<Long> userIds = commentPage.allComments().stream()
                .map(Comment::getUserId)
                .distinct()
                .toList();
        Map<Long, AuthorInfo> authorMap = userPort.findAuthorsByIds(userIds);

        List<AdminPostDetailResult.CommentResult> comments = buildCommentTree(
                commentPage.parentComments(),
                commentPage.replies(),
                authorMap
        );

        return new AdminPostCommentsResult(
                comments,
                commentPage.hasNext(),
                commentPage.nextCursor()
        );
    }

    private AdminCommentPage fetchCommentsWithTree(Long postId, Long cursor) {
        List<Comment> parentComments = commentReadService.findParentComments(postId, cursor, COMMENT_PAGE_SIZE + 1);
        boolean hasNext = parentComments.size() > COMMENT_PAGE_SIZE;
        if (hasNext) {
            parentComments = parentComments.subList(0, COMMENT_PAGE_SIZE);
        }

        Long nextCursor = hasNext && !parentComments.isEmpty()
                ? parentComments.getLast().getId()
                : null;

        List<Long> parentIds = parentComments.stream().map(Comment::getId).toList();
        List<Comment> replies = commentReadService.findRepliesByParentIds(postId, parentIds);
        List<Comment> allComments = Stream.concat(parentComments.stream(), replies.stream()).toList();

        return new AdminCommentPage(parentComments, replies, allComments, hasNext, nextCursor);
    }

    private List<AdminPostDetailResult.CommentResult> buildCommentTree(
            List<Comment> parentComments,
            List<Comment> replies,
            Map<Long, AuthorInfo> authorMap
    ) {
        Map<Long, List<Comment>> repliesMap = replies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        return parentComments.stream()
                .map(parent -> buildCommentDto(
                        parent,
                        authorMap,
                        parent.getStatus() == Status.DELETED,
                        repliesMap.getOrDefault(parent.getId(), List.of()).stream()
                                .map(reply -> buildReplyDto(
                                        reply,
                                        authorMap,
                                        reply.getStatus() == Status.DELETED
                                ))
                                .toList()
                ))
                .toList();
    }

    private AdminPostDetailResult.CommentResult buildCommentDto(
            Comment comment,
            Map<Long, AuthorInfo> authorMap,
            boolean deleted,
            List<AdminPostDetailResult.ReplyResult> replies
    ) {
        AuthorInfo author = authorMap.getOrDefault(comment.getUserId(), AuthorInfo.unknown(comment.getUserId()));

        return new AdminPostDetailResult.CommentResult(
                comment.getId(),
                deleted ? null : comment.getUserId(),
                deleted ? null : author.getNickname(),
                deleted ? null : author.getTeamCode(),
                deleted ? "삭제된 댓글입니다" : comment.getContent(),
                deleted ? 0 : comment.getLikeCount(),
                comment.getDepth(),
                comment.getCreatedAt(),
                false,
                deleted,
                replies
        );
    }

    private AdminPostDetailResult.ReplyResult buildReplyDto(
            Comment comment,
            Map<Long, AuthorInfo> authorMap,
            boolean deleted
    ) {
        AuthorInfo author = authorMap.getOrDefault(comment.getUserId(), AuthorInfo.unknown(comment.getUserId()));

        return new AdminPostDetailResult.ReplyResult(
                comment.getId(),
                deleted ? null : comment.getUserId(),
                deleted ? null : author.getNickname(),
                deleted ? null : author.getTeamCode(),
                deleted ? "삭제된 댓글입니다" : comment.getContent(),
                deleted ? 0 : comment.getLikeCount(),
                comment.getDepth(),
                comment.getCreatedAt(),
                false,
                deleted
        );
    }

    private Map<Long, List<AdminPostDetailResult.ImageResult>> getImagesMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        List<PostImage> images = postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(postIds, Status.ACTIVE);
        return images.stream()
                .collect(Collectors.groupingBy(
                        PostImage::getPostId,
                        Collectors.mapping(
                                image -> new AdminPostDetailResult.ImageResult(
                                        image.getId(),
                                        image.getImgUrl()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private Map<Long, List<String>> getHashtagsMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        List<PostHashtag> postHashtags = postHashtagJpaRepository.findByPost_IdIn(postIds);
        return postHashtags.stream()
                .collect(Collectors.groupingBy(
                        postHashtag -> postHashtag.getPost().getId(),
                        Collectors.mapping(postHashtag -> postHashtag.getHashtag().getTagName(), Collectors.toList())
                ));
    }

    private record AdminCommentPage(
            List<Comment> parentComments,
            List<Comment> replies,
            List<Comment> allComments,
            boolean hasNext,
            Long nextCursor
    ) {
    }
}
