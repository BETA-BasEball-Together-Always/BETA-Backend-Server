package com.beta.community.application.admin.dto;

import java.util.List;

public record AdminPostCommentsResult(
        List<AdminPostDetailResult.CommentResult> comments,
        boolean hasNext,
        Long nextCursor
) {
}
