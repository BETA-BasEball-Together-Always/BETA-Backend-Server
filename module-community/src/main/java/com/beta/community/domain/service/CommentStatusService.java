package com.beta.community.domain.service;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Status;
import com.beta.core.exception.admin.InvalidAdminActionException;
import org.springframework.stereotype.Service;

@Service
public class CommentStatusService {

    public void validateHide(Comment comment) {
        if (comment.getStatus() == Status.DELETED) {
            throw new InvalidAdminActionException("삭제된 댓글은 숨김 처리할 수 없습니다.");
        }

        if (comment.getStatus() != Status.ACTIVE) {
            throw new InvalidAdminActionException("노출 중인 댓글만 숨길 수 있습니다.");
        }
    }

    public void validateUnhide(Comment comment) {
        if (comment.getStatus() == Status.DELETED) {
            throw new InvalidAdminActionException("삭제된 댓글은 다시 노출할 수 없습니다.");
        }

        if (comment.getStatus() != Status.HIDDEN) {
            throw new InvalidAdminActionException("숨김 상태 댓글만 다시 노출할 수 있습니다.");
        }
    }
}
