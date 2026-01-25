package com.beta.community.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "post_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseEntity {

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "img_url", nullable = false)
    private String imgUrl;

    @Column(name = "origin_name", length = 100)
    private String originName;

    @Column(name = "new_name", length = 100)
    private String newName;

    @Column(name = "sort", nullable = false)
    private Integer sort = 0;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Builder
    public PostImage(Long postId, Long userId, String imgUrl, String originName, String newName,
                           Integer sort, Long fileSize, String mimeType, Status status) {
        this.postId = postId;
        this.userId = userId;
        this.imgUrl = imgUrl;
        this.originName = originName;
        this.newName = newName;
        this.sort = sort != null ? sort : 0;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        if (status != null) {
            this.status = status;
        }
    }

    public void softDelete() {
        this.status = Status.DELETED;
    }
}
