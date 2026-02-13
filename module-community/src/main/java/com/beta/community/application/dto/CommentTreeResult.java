package com.beta.community.application.dto;

import com.beta.community.domain.entity.Comment;

import java.util.List;
import java.util.Set;

public record CommentTreeResult(
        List<Comment> parentComments,
        List<Comment> replies,
        List<Comment> allComments,
        Set<Long> likedCommentIds,
        boolean hasNext,
        Long nextCursor
) {
}
