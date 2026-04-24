package com.beta.community.application;

import com.beta.community.infra.repository.NotificationThrottleRedisRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class NotificationThrottleService {

    private static final String POST_COMMENT_PREFIX = "POST_COMMENT";
    private static final String POST_EMOTION_PREFIX = "POST_EMOTION";
    private static final Duration COMMENT_THROTTLE_DURATION = Duration.ofMinutes(1);
    private static final Duration EMOTION_THROTTLE_DURATION = Duration.ofMinutes(5);

    private final NotificationThrottleRedisRepository notificationThrottleRedisRepository;

    public NotificationThrottleService(NotificationThrottleRedisRepository notificationThrottleRedisRepository) {
        this.notificationThrottleRedisRepository = notificationThrottleRedisRepository;
    }

    public boolean canSendPostComment(Long actorUserId, Long targetUserId, Long postId) {
        return tryAcquire(buildKey(POST_COMMENT_PREFIX, actorUserId, targetUserId, postId), COMMENT_THROTTLE_DURATION);
    }

    public boolean canSendPostEmotion(Long actorUserId, Long targetUserId, Long postId) {
        return tryAcquire(buildKey(POST_EMOTION_PREFIX, actorUserId, targetUserId, postId), EMOTION_THROTTLE_DURATION);
    }

    String buildKey(String type, Long actorUserId, Long targetUserId, Long postId) {
        return "notification:throttle:" + type + ":" + actorUserId + ":" + targetUserId + ":" + postId;
    }

    boolean tryAcquire(String key, Duration window) {
        return notificationThrottleRedisRepository.tryAcquire(key, window);
    }
}
