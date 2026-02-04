package com.beta.community.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "sad_count", nullable = false)
    private Integer sadCount = 0;

    @Column(name = "fun_count", nullable = false)
    private Integer funCount = 0;

    @Column(name = "hype_count", nullable = false)
    private Integer hypeCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @Builder
    public Post(Long userId, String content, String channel) {
        this.userId = userId;
        this.content = content;
        this.channel = Channel.valueOf(channel);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }

    public void softDelete() {
        this.status = Status.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public enum Channel {
        DOOSAN,
        LG,
        KIWOOM,
        KT,
        SSG,
        KIA,
        SAMSUNG,
        NC,
        HANWHA,
        LOTTE,
        ALL
    }
}
