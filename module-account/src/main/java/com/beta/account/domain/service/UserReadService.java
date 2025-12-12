package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.core.exception.account.EmailDuplicateException;
import com.beta.core.exception.account.NameDuplicateException;
import com.beta.core.exception.account.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReadService {

    private final UserJpaRepository userJpaRepository;

    public User findUserBySocialId(String socialId, SocialProvider socialProvider) {
        return userJpaRepository.findBySocialIdAndSocialProvider(socialId, socialProvider).orElse(null);
    }

    public User findUserByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 가입된 사용자가 없습니다."));
    }

    public User findUserById(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
    }
}
