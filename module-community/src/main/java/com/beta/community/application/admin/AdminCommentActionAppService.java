package com.beta.community.application.admin;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.service.CommentReadService;
import com.beta.community.domain.service.CommentStatusService;
import com.beta.community.domain.service.CommentWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCommentActionAppService {

    private final CommentReadService commentReadService;
    private final CommentStatusService commentStatusService;
    private final CommentWriteService commentWriteService;

    @Transactional
    public void hideComment(Long targetCommentId) {
        Comment comment = commentReadService.findById(targetCommentId);
        commentStatusService.validateHide(comment);
        commentWriteService.hide(comment);
    }

    @Transactional
    public void unhideComment(Long targetCommentId) {
        Comment comment = commentReadService.findById(targetCommentId);
        commentStatusService.validateUnhide(comment);
        commentWriteService.unhide(comment);
    }
}
