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

    @Column(nullable = false, unique = true, length = 36)
    private String deviceId;

    @Column(length = 500)
    private String fcmToken;

    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Boolean pushEnabled;

    @Builder
    public UserDevice(Long userId, String deviceId, String fcmToken) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.lastUsedAt = LocalDateTime.now();
        this.isActive = true;
        this.pushEnabled = null;
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

    public void updatePushSettings(String fcmToken, Boolean pushEnabled) {
        this.fcmToken = fcmToken;
        this.pushEnabled = pushEnabled;
    }

    public void updatePushEnabled(Boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }
}
