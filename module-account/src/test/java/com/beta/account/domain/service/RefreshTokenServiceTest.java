package com.beta.account.domain.service;

import com.beta.account.infra.repository.RefreshTokenRedisRepository;
import com.beta.core.exception.ErrorCode;
import com.beta.core.exception.account.InvalidTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService 단위 테스트")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("리프레시 토큰을 저장한다")
    void upsertRefreshToken_Success() {
        // given
        Long userId = 1L;
        String refreshToken = "test-refresh-token";

        // when
        refreshTokenService.upsertRefreshToken(userId, refreshToken);

        // then
        verify(refreshTokenRedisRepository, times(1)).save(userId, refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰으로 사용자 ID를 조회한다")
    void findUserIdByToken_Success_WhenTokenExists() {
        // given
        String refreshToken = "valid-refresh-token";
        Long expectedUserId = 1L;
        when(refreshTokenRedisRepository.findUserIdByToken(refreshToken))
                .thenReturn(Optional.of(expectedUserId));

        // when
        Long actualUserId = refreshTokenService.findUserIdByToken(refreshToken);

        // then
        assertThat(actualUserId).isEqualTo(expectedUserId);
        verify(refreshTokenRedisRepository, times(1)).findUserIdByToken(refreshToken);
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 조회 시 InvalidTokenException을 발생시킨다")
    void findUserIdByToken_ThrowsInvalidTokenException_WhenTokenNotFound() {
        // given
        String invalidRefreshToken = "invalid-refresh-token";
        when(refreshTokenRedisRepository.findUserIdByToken(invalidRefreshToken))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.findUserIdByToken(invalidRefreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효하지 않거나 만료된 리프레시 토큰입니다.")
                .satisfies(exception -> {
                    InvalidTokenException ex = (InvalidTokenException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                });

        verify(refreshTokenRedisRepository, times(1)).findUserIdByToken(invalidRefreshToken);
    }

    @Test
    @DisplayName("사용자 ID로 리프레시 토큰을 삭제한다")
    void deleteByUserId_Success() {
        // given
        Long userId = 1L;

        // when
        refreshTokenService.deleteByUserId(userId);

        // then
        verify(refreshTokenRedisRepository, times(1)).deleteByUserId(userId);
    }

    @Test
    @DisplayName("리프레시 토큰 저장 시 기존 토큰을 덮어쓴다 (upsert)")
    void upsertRefreshToken_OverwritesExistingToken() {
        // given
        Long userId = 1L;
        String oldToken = "old-refresh-token";
        String newToken = "new-refresh-token";

        // when
        refreshTokenService.upsertRefreshToken(userId, oldToken);
        refreshTokenService.upsertRefreshToken(userId, newToken);

        // then
        verify(refreshTokenRedisRepository, times(2)).save(anyLong(), anyString());
    }
}
