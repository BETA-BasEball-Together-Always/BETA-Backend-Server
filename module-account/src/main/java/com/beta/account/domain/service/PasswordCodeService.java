package com.beta.account.domain.service;

import com.beta.account.infra.repository.UserRedisRepository;
import com.beta.core.exception.account.InvalidVerificationCodeException;
import com.beta.core.exception.account.PasswordCodeCooldownException;
import com.beta.core.exception.account.VerificationCodeExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.util.concurrent.ThreadLocalRandom.current;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordCodeService {

    private final UserRedisRepository userRedisRepository;

    public String generateAndSaveVerificationCode(Long userId) {
        if (userRedisRepository.isCooldownActive(userId)) {
            Long remainingSeconds = userRedisRepository.getCooldownTTL(userId);
            throw new PasswordCodeCooldownException("인증코드 재전송은 1분 후에 가능합니다");
        }

        userRedisRepository.deletePasswordCode(userId);
        String verificationCode = String.format("%06d", current().nextInt(1_000_000));

        userRedisRepository.savePasswordCode(userId, verificationCode);
        userRedisRepository.saveCooldown(userId);
        return verificationCode;
    }

    public void verifyCode(Long userId, String code) {
        String savedCode = userRedisRepository.getPasswordCode(userId);

        if (savedCode == null) {
            throw new VerificationCodeExpiredException("인증코드가 만료되었습니다");
        }

        if (!savedCode.equals(code)) {
            throw new InvalidVerificationCodeException("인증코드가 일치하지 않습니다");
        }
    }

    public void deleteCode(Long userId) {
        userRedisRepository.deletePasswordCode(userId);
    }
}
