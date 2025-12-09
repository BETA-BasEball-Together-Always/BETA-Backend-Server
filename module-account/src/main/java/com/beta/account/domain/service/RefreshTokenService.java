package com.beta.account.domain.service;

import com.beta.account.infra.repository.RefreshTokenRedisRepository;
import com.beta.core.exception.account.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public void upsertRefreshToken(Long userId, String refreshToken) {
        refreshTokenRedisRepository.save(userId, refreshToken);
    }

    public Long findUserIdByToken(String refreshToken) {
        return refreshTokenRedisRepository.findUserIdByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않거나 만료된 리프레시 토큰입니다."));
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRedisRepository.deleteByUserId(userId);
    }
}
