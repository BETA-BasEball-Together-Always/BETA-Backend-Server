package com.beta.community.application;

import com.beta.community.infra.repository.NotificationThrottleRedisRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationThrottleServiceTest {

    @Test
    void 같은_사용자가_같은_게시글에_댓글_알림을_연속_시도하면_제한_시간_내_두_번째_요청은_차단한다() {
        // given
        NotificationThrottleRedisRepository notificationThrottleRedisRepository = mock(NotificationThrottleRedisRepository.class);
        NotificationThrottleService notificationThrottleService =
                new NotificationThrottleService(notificationThrottleRedisRepository);

        when(notificationThrottleRedisRepository.tryAcquire(
                "notification:throttle:POST_COMMENT:1:2:3",
                Duration.ofMinutes(1)
        )).thenReturn(true, false);

        // when
        boolean firstAttempt = notificationThrottleService.canSendPostComment(1L, 2L, 3L);
        boolean secondAttempt = notificationThrottleService.canSendPostComment(1L, 2L, 3L);

        // then
        assertThat(firstAttempt).isTrue();
        assertThat(secondAttempt).isFalse();
    }

    @Test
    void 다른_사용자의_댓글_알림은_같은_제한_시간_안에서도_각각_허용한다() {
        // given
        NotificationThrottleRedisRepository notificationThrottleRedisRepository = mock(NotificationThrottleRedisRepository.class);
        NotificationThrottleService notificationThrottleService =
                new NotificationThrottleService(notificationThrottleRedisRepository);

        when(notificationThrottleRedisRepository.tryAcquire(
                "notification:throttle:POST_COMMENT:1:2:3",
                Duration.ofMinutes(1)
        )).thenReturn(true);
        when(notificationThrottleRedisRepository.tryAcquire(
                "notification:throttle:POST_COMMENT:4:2:3",
                Duration.ofMinutes(1)
        )).thenReturn(true);

        // when
        boolean firstActorAttempt = notificationThrottleService.canSendPostComment(1L, 2L, 3L);
        boolean secondActorAttempt = notificationThrottleService.canSendPostComment(4L, 2L, 3L);

        // then
        assertThat(firstActorAttempt).isTrue();
        assertThat(secondActorAttempt).isTrue();
    }

    @Test
    void 같은_사용자가_같은_게시글에_공감_알림을_연속_시도하면_제한_시간_내_두_번째_요청은_차단한다() {
        // given
        NotificationThrottleRedisRepository notificationThrottleRedisRepository = mock(NotificationThrottleRedisRepository.class);
        NotificationThrottleService notificationThrottleService =
                new NotificationThrottleService(notificationThrottleRedisRepository);

        when(notificationThrottleRedisRepository.tryAcquire(
                "notification:throttle:POST_EMOTION:1:2:3",
                Duration.ofMinutes(5)
        )).thenReturn(true);

        // when
        boolean allowed = notificationThrottleService.canSendPostEmotion(1L, 2L, 3L);

        // then
        assertThat(allowed).isTrue();
        verify(notificationThrottleRedisRepository).tryAcquire(
                "notification:throttle:POST_EMOTION:1:2:3",
                Duration.ofMinutes(5)
        );
    }
}
