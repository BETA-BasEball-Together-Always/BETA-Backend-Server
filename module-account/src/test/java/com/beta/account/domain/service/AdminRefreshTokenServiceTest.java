package com.beta.account.domain.service;

import com.beta.account.infra.repository.AdminRefreshTokenRedisRepository;
import com.beta.core.exception.ErrorCode;
import com.beta.core.exception.account.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRefreshTokenServiceTest {

    @Mock
    private AdminRefreshTokenRedisRepository adminRefreshTokenRedisRepository;

    @InjectMocks
    private AdminRefreshTokenService adminRefreshTokenService;

    @Test
    void 관리자_리프레시_토큰을_저장한다() {
        // given
        Long userId = 1L;
        String refreshToken = "admin-refresh-token";

        // when
        adminRefreshTokenService.upsertRefreshToken(userId, refreshToken);

        // then
        verify(adminRefreshTokenRedisRepository, times(1)).save(userId, refreshToken);
    }

    @Test
    void 관리자_리프레시_토큰으로_사용자_ID를_조회한다() {
        // given
        String refreshToken = "valid-admin-refresh-token";
        Long expectedUserId = 1L;
        when(adminRefreshTokenRedisRepository.findUserIdByToken(refreshToken))
                .thenReturn(Optional.of(expectedUserId));

        // when
        Long actualUserId = adminRefreshTokenService.findUserIdByToken(refreshToken);

        // then
        assertThat(actualUserId).isEqualTo(expectedUserId);
        verify(adminRefreshTokenRedisRepository, times(1)).findUserIdByToken(refreshToken);
    }

    @Test
    void 존재하지_않는_관리자_리프레시_토큰으로_조회_시_InvalidTokenException_예외를_반환한다() {
        // given
        String invalidRefreshToken = "invalid-admin-refresh-token";
        when(adminRefreshTokenRedisRepository.findUserIdByToken(invalidRefreshToken))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminRefreshTokenService.findUserIdByToken(invalidRefreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .satisfies(exception -> {
                    InvalidTokenException ex = (InvalidTokenException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                });

        verify(adminRefreshTokenRedisRepository, times(1)).findUserIdByToken(invalidRefreshToken);
    }

    @Test
    void 사용자_ID로_어드민_리프레시_토큰을_삭제한다() {
        // given
        Long userId = 1L;

        // when
        adminRefreshTokenService.deleteByUserId(userId);

        // then
        verify(adminRefreshTokenRedisRepository, times(1)).deleteByUserId(userId);
    }

    @Test
    void 어드민_리프레시_토큰_저장_시_기존_토큰을_덮어쓴다() {
        // given
        Long userId = 1L;
        String oldToken = "old-admin-refresh-token";
        String newToken = "new-admin-refresh-token";

        // when
        adminRefreshTokenService.upsertRefreshToken(userId, oldToken);
        adminRefreshTokenService.upsertRefreshToken(userId, newToken);

        // then
        verify(adminRefreshTokenRedisRepository, times(2)).save(anyLong(), anyString());
    }
}
