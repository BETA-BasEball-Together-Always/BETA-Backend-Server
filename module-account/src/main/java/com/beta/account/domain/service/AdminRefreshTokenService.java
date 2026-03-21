package com.beta.account.domain.service;

import com.beta.account.infra.repository.AdminRefreshTokenRedisRepository;
import com.beta.core.exception.account.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminRefreshTokenService {

    private final AdminRefreshTokenRedisRepository adminRefreshTokenRedisRepository;

    public void upsertRefreshToken(Long userId, String refreshToken) {
        adminRefreshTokenRedisRepository.save(userId, refreshToken);
    }

    public Long findUserIdByToken(String refreshToken) {
        return adminRefreshTokenRedisRepository.findUserIdByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 관리자 리프레시 토큰입니다."));
    }

    public void deleteByUserId(Long userId) {
        adminRefreshTokenRedisRepository.deleteByUserId(userId);
    }
}
