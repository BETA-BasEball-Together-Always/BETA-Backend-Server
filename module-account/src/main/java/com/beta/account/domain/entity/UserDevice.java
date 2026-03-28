package com.beta.account.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"userId", "deviceId"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDevice extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 36)
    private String deviceId;

    @Column(length = 500)
    private String fcmToken;

    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Boolean pushEnabled;

    @Column(name = "post_comment_push_enabled")
    private Boolean postCommentPushEnabled;

    @Column(name = "post_emotion_push_enabled")
    private Boolean postEmotionPushEnabled;

    @Builder
    public UserDevice(Long userId, String deviceId, String fcmToken) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.lastUsedAt = LocalDateTime.now();
        this.isActive = true;
        this.pushEnabled = null;
        this.postCommentPushEnabled = null;
        this.postEmotionPushEnabled = null;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updatePushEnabled(Boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
        this.postCommentPushEnabled = pushEnabled;
        this.postEmotionPushEnabled = pushEnabled;
    }

    public void updatePushDetailSettings(Boolean postCommentPushEnabled, Boolean postEmotionPushEnabled) {
        this.postCommentPushEnabled = postCommentPushEnabled;
        this.postEmotionPushEnabled = postEmotionPushEnabled;
    }
}
