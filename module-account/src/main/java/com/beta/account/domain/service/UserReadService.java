package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserReadService {

    private final UserJpaRepository userJpaRepository;

    @Transactional(readOnly = true)
    public User findUserBySocialId(String socialId, SocialProvider socialProvider) {
        return userJpaRepository.findBySocialIdAndSocialProvider(socialId, socialProvider).orElse(null);
    }
}
