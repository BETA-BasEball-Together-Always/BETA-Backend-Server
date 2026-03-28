package com.beta.account.adapter;

import com.beta.account.domain.entity.UserDevice;
import com.beta.account.domain.service.UserDeviceReadService;
import com.beta.core.port.PushPort;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnBean(FirebaseMessaging.class)
@RequiredArgsConstructor
public class PushPortAdapter implements PushPort {

    private static final String COMMENT_TITLE = "내 게시글에 새 댓글이 달렸어요";
    private static final String EMOTION_TITLE = "내 게시글에 새 공감이 도착했어요";

    private final UserDeviceReadService userDeviceReadService;
    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendPostCommentNotification(Long targetUserId, String actorNickname, Long postId, Long commentId) {
        List<UserDevice> devices = userDeviceReadService.findCommentPushEnabledDevicesByUserId(targetUserId);
        if (devices.isEmpty()) {
            return;
        }

        String body = actorNickname + "님이 회원님의 게시글에 댓글을 남겼어요.";
        Map<String, String> data = Map.of(
                "type", "POST_COMMENT",
                "postId", String.valueOf(postId),
                "commentId", String.valueOf(commentId)
        );

        sendToDevices(devices, COMMENT_TITLE, body, data);
    }

    @Override
    public void sendPostEmotionNotification(Long targetUserId, String actorNickname, String emotionType, Long postId) {
        List<UserDevice> devices = userDeviceReadService.findEmotionPushEnabledDevicesByUserId(targetUserId);
        if (devices.isEmpty()) {
            return;
        }

        String body = actorNickname + "님이 회원님의 게시글에 " + toEmotionLabel(emotionType) + " 반응을 남겼어요.";
        Map<String, String> data = Map.of(
                "type", "POST_EMOTION",
                "postId", String.valueOf(postId),
                "emotionType", emotionType
        );

        sendToDevices(devices, EMOTION_TITLE, body, data);
    }

    private void sendToDevices(List<UserDevice> devices, String title, String body, Map<String, String> data) {
        for (UserDevice device : devices) {
            try {
                Message message = Message.builder()
                        .setToken(device.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setApnsConfig(buildApnsConfig(title, body))
                        .putAllData(data)
                        .build();

                firebaseMessaging.send(message);
            } catch (Exception e) {
                log.warn("푸시 알림 발송 실패 userId={}, deviceId={}, reason={}",
                        device.getUserId(), device.getDeviceId(), e.getMessage());
            }
        }
    }

    private String toEmotionLabel(String emotionType) {
        return switch (emotionType) {
            case "LIKE" -> "좋아요";
            case "SAD" -> "슬퍼요";
            case "FUN" -> "재밌어요";
            case "HYPE" -> "신나요";
            default -> "공감";
        };
    }

    private ApnsConfig buildApnsConfig(String title, String body) {
        return ApnsConfig.builder()
                .putHeader("apns-push-type", "alert")
                .putHeader("apns-priority", "10")
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setSound("default")
                        .build())
                .build();
    }
}
