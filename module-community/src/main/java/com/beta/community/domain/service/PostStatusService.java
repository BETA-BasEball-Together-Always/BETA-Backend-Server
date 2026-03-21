package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import com.beta.core.exception.admin.InvalidAdminActionException;
import org.springframework.stereotype.Service;

@Service
public class PostStatusService {

    public void validateHide(Post post) {
        if (post.getStatus() == Status.DELETED) {
            throw new InvalidAdminActionException("삭제된 게시글은 숨김 처리할 수 없습니다.");
        }

        if (post.getStatus() != Status.ACTIVE) {
            throw new InvalidAdminActionException("노출 중인 게시글만 숨길 수 있습니다.");
        }
    }

    public void validateUnhide(Post post) {
        if (post.getStatus() == Status.DELETED) {
            throw new InvalidAdminActionException("삭제된 게시글은 다시 노출할 수 없습니다.");
        }

        if (post.getStatus() != Status.HIDDEN) {
            throw new InvalidAdminActionException("숨김 상태 게시글만 다시 노출할 수 있습니다.");
        }
    }
}
