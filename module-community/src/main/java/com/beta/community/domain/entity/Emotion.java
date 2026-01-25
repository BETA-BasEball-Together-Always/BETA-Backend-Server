package com.beta.community.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "emotion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emotion extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "emotion_type", nullable = false)
    private EmotionType emotionType;

    @Builder
    public Emotion(Long userId, Long postId, EmotionType emotionType) {
        this.userId = userId;
        this.postId = postId;
        this.emotionType = emotionType;
    }

    public void changeEmotionType(EmotionType emotionType) {
        this.emotionType = emotionType;
    }

    @Getter
    public enum EmotionType {
        LIKE,
        SAD,
        FUN,
        HYPE
    }
}
