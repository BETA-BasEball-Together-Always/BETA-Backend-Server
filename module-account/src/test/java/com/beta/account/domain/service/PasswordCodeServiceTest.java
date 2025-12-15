package com.beta.account.domain.service;

import com.beta.account.infra.repository.UserRedisRepository;
import com.beta.core.exception.account.InvalidVerificationCodeException;
import com.beta.core.exception.account.PasswordCodeCooldownException;
import com.beta.core.exception.account.VerificationCodeExpiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordCodeService 단위 테스트")
class PasswordCodeServiceTest {

    @Mock
    private UserRedisRepository userRedisRepository;

    @InjectMocks
    private PasswordCodeService passwordCodeService;

    @Test
    @DisplayName("인증코드를 생성하고 Redis에 저장한다")
    void generateAndSaveVerificationCode_Success() {
        // given
        Long userId = 1L;
        when(userRedisRepository.isCooldownActive(userId)).thenReturn(false);

        // when
        String code = passwordCodeService.generateAndSaveVerificationCode(userId);

        // then
        assertThat(code).isNotNull();
        assertThat(code).hasSize(6);
        assertThat(code).matches("\\d{6}");  // 6자리 숫자 확인

        verify(userRedisRepository).deletePasswordCode(userId);
        verify(userRedisRepository).savePasswordCode(eq(userId), eq(code));
        verify(userRedisRepository).saveCooldown(userId);
    }

    @Test
    @DisplayName("쿨다운 중에 인증코드 재발급 시 PasswordCodeCooldownException 발생")
    void generateAndSaveVerificationCode_ThrowsException_WhenCooldownActive() {
        // given
        Long userId = 1L;
        when(userRedisRepository.isCooldownActive(userId)).thenReturn(true);
        when(userRedisRepository.getCooldownTTL(userId)).thenReturn(45L);

        // when & then
        assertThatThrownBy(() -> passwordCodeService.generateAndSaveVerificationCode(userId))
                .isInstanceOf(PasswordCodeCooldownException.class)
                .hasMessage("인증코드 재전송은 1분 후에 가능합니다");

        verify(userRedisRepository).isCooldownActive(userId);
        verify(userRedisRepository, never()).savePasswordCode(any(), any());
    }

    @Test
    @DisplayName("올바른 인증코드로 검증 시 성공")
    void verifyCode_Success() {
        // given
        Long userId = 1L;
        String correctCode = "123456";
        when(userRedisRepository.getPasswordCode(userId)).thenReturn(correctCode);

        // when & then
        passwordCodeService.verifyCode(userId, correctCode);

        verify(userRedisRepository).getPasswordCode(userId);
    }

    @Test
    @DisplayName("인증코드가 만료되었을 때 VerificationCodeExpiredException 발생")
    void verifyCode_ThrowsException_WhenCodeExpired() {
        // given
        Long userId = 1L;
        String code = "123456";
        when(userRedisRepository.getPasswordCode(userId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> passwordCodeService.verifyCode(userId, code))
                .isInstanceOf(VerificationCodeExpiredException.class)
                .hasMessage("인증코드가 만료되었습니다");

        verify(userRedisRepository).getPasswordCode(userId);
    }

    @Test
    @DisplayName("잘못된 인증코드로 검증 시 InvalidVerificationCodeException 발생")
    void verifyCode_ThrowsException_WhenCodeInvalid() {
        // given
        Long userId = 1L;
        String savedCode = "123456";
        String wrongCode = "654321";
        when(userRedisRepository.getPasswordCode(userId)).thenReturn(savedCode);

        // when & then
        assertThatThrownBy(() -> passwordCodeService.verifyCode(userId, wrongCode))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .hasMessage("인증코드가 일치하지 않습니다");

        verify(userRedisRepository).getPasswordCode(userId);
    }

    @Test
    @DisplayName("인증코드를 삭제한다")
    void deleteCode_Success() {
        // given
        Long userId = 1L;

        // when
        passwordCodeService.deleteCode(userId);

        // then
        verify(userRedisRepository).deletePasswordCode(userId);
    }

    @Test
    @DisplayName("생성된 인증코드는 항상 6자리 숫자다")
    void generateAndSaveVerificationCode_AlwaysGenerates6DigitCode() {
        // given
        Long userId = 1L;
        when(userRedisRepository.isCooldownActive(userId)).thenReturn(false);

        // when
        for (int i = 0; i < 10; i++) {
            String code = passwordCodeService.generateAndSaveVerificationCode(userId);

            // then
            assertThat(code).hasSize(6);
            assertThat(code).matches("\\d{6}");
            int codeValue = Integer.parseInt(code);
            assertThat(codeValue).isBetween(0, 999999);
        }
    }

    @Test
    @DisplayName("쿨다운 중일 때 Redis 저장 메서드가 호출되지 않는다")
    void generateAndSaveVerificationCode_DoesNotSaveWhenCooldownActive() {
        // given
        Long userId = 1L;
        when(userRedisRepository.isCooldownActive(userId)).thenReturn(true);

        // when & then
        try {
            passwordCodeService.generateAndSaveVerificationCode(userId);
        } catch (PasswordCodeCooldownException e) {
            // 예외 발생 예상
        }

        verify(userRedisRepository, never()).deletePasswordCode(userId);
        verify(userRedisRepository, never()).savePasswordCode(any(), any());
        verify(userRedisRepository, never()).saveCooldown(userId);
    }
}
