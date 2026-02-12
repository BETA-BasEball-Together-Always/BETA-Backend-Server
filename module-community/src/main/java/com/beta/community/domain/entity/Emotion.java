package com.beta.community.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "emotion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_emotion_user_post", columnNames = {"user_id", "post_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emotion extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion_type", nullable = false, length = 10)
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
