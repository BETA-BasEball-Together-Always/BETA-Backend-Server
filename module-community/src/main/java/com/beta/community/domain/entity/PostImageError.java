package com.beta.community.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_image_error")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImageError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column
    private String fileName;

    @Column(name = "error_at", nullable = false)
    private LocalDateTime errorAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public PostImageError(String imageUrl, String fileName, Long userId) {
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.userId = userId;
        this.errorAt = LocalDateTime.now();
    }

    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
    }
}
