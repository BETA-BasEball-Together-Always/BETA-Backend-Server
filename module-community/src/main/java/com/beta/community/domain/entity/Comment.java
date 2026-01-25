package com.beta.community.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "depth", nullable = false)
    private Integer depth = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Comment(Long postId, Long userId, String content, Long parentId, Integer depth, Integer likeCount) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.parentId = parentId;
        this.depth = depth != null ? depth : 0;
        this.likeCount = likeCount != null ? likeCount : 0;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.status = Status.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
