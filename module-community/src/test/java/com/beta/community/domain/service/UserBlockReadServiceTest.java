package com.beta.community.domain.service;

import com.beta.community.domain.entity.UserBlock;
import com.beta.community.infra.repository.UserBlockJpaRepository;
import com.beta.core.exception.community.BlockNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserBlockReadService 단위 테스트")
class UserBlockReadServiceTest {

    @Mock
    private UserBlockJpaRepository userBlockJpaRepository;

    @InjectMocks
    private UserBlockReadService userBlockReadService;

    @Nested
    @DisplayName("findByBlockerAndBlocked")
    class FindByBlockerAndBlocked {

        @Test
        @DisplayName("존재하는 차단 정보를 조회한다")
        void success() {
            // given
            Long blockerId = 1L;
            Long blockedId = 2L;
            UserBlock userBlock = UserBlock.builder()
                    .blockerId(blockerId)
                    .blockedId(blockedId)
                    .build();
            when(userBlockJpaRepository.findByBlockerIdAndBlockedId(blockerId, blockedId))
                    .thenReturn(Optional.of(userBlock));

            // when
            UserBlock result = userBlockReadService.findByBlockerAndBlocked(blockerId, blockedId);

            // then
            assertThat(result.getBlockerId()).isEqualTo(blockerId);
            assertThat(result.getBlockedId()).isEqualTo(blockedId);
        }

        @Test
        @DisplayName("존재하지 않는 차단 정보 조회 시 BlockNotFoundException 발생")
        void throwException_whenNotFound() {
            // given
            Long blockerId = 1L;
            Long blockedId = 999L;
            when(userBlockJpaRepository.findByBlockerIdAndBlockedId(blockerId, blockedId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userBlockReadService.findByBlockerAndBlocked(blockerId, blockedId))
                    .isInstanceOf(BlockNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("isBlocked")
    class IsBlocked {

        @Test
        @DisplayName("차단된 경우 true를 반환한다")
        void returnsTrue_whenBlocked() {
            // given
            Long blockerId = 1L;
            Long blockedId = 2L;
            when(userBlockJpaRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId))
                    .thenReturn(true);

            // when
            boolean result = userBlockReadService.isBlocked(blockerId, blockedId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("차단되지 않은 경우 false를 반환한다")
        void returnsFalse_whenNotBlocked() {
            // given
            Long blockerId = 1L;
            Long blockedId = 2L;
            when(userBlockJpaRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId))
                    .thenReturn(false);

            // when
            boolean result = userBlockReadService.isBlocked(blockerId, blockedId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findBlockedUserIds")
    class FindBlockedUserIds {

        @Test
        @DisplayName("차단한 사용자 ID 목록을 반환한다")
        void success() {
            // given
            Long blockerId = 1L;
            List<UserBlock> userBlocks = List.of(
                    UserBlock.builder().blockerId(blockerId).blockedId(2L).build(),
                    UserBlock.builder().blockerId(blockerId).blockedId(3L).build(),
                    UserBlock.builder().blockerId(blockerId).blockedId(5L).build()
            );
            when(userBlockJpaRepository.findAllByBlockerId(blockerId))
                    .thenReturn(userBlocks);

            // when
            List<Long> result = userBlockReadService.findBlockedUserIds(blockerId);

            // then
            assertThat(result).containsExactly(2L, 3L, 5L);
        }

        @Test
        @DisplayName("차단한 사용자가 없는 경우 빈 리스트를 반환한다")
        void returnsEmptyList_whenNoBlocked() {
            // given
            Long blockerId = 1L;
            when(userBlockJpaRepository.findAllByBlockerId(blockerId))
                    .thenReturn(Collections.emptyList());

            // when
            List<Long> result = userBlockReadService.findBlockedUserIds(blockerId);

            // then
            assertThat(result).isEmpty();
        }
    }
}
