package com.beta.community.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "comment_like", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_like_user_comment", columnNames = {"user_id", "comment_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Builder
    public CommentLike(Long userId, Long commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }
}
