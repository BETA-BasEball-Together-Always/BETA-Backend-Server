package com.beta.community.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentsDto {

    private List<PostDetailDto.CommentDto> comments;
    private boolean hasNext;
    private Long nextCursor;
}
