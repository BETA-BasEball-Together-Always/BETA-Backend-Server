package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.entity.UserConsents;
import com.beta.account.infra.repository.UserConsentJpaRepository;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.core.exception.account.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWriteService {

    private final UserJpaRepository userJpaRepository;
    private final UserConsentJpaRepository userConsentJpaRepository;

    @Transactional
    public UserDto saveUser(User user) {
        return UserDto.toDto(userJpaRepository.save(user));
    }

    @Transactional
    public User createSocialUser(String socialId, SocialProvider socialProvider) {
        User user = User.createNewSocialUser(socialId, socialProvider);
        return userJpaRepository.save(user);
    }

    @Transactional
    public User processConsent(Long userId, Boolean agreeMarketing, Boolean personalInfoRequired) {
        User user = userJpaRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        user.agreeConsent();
        userConsentJpaRepository.save(UserConsents.builder()
                .userId(userId)
                .agreeMarketing(agreeMarketing)
                .personalInfoRequired(personalInfoRequired)
                .build());
        return user;
    }

    @Transactional
    public User updateProfile(Long userId, String email, String nickname) {
        User user = userJpaRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        user.updateProfile(email, nickname);
        return user;
    }

    @Transactional
    public User updateTeam(Long userId, BaseballTeam baseballTeam) {
        User user = userJpaRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        user.updateTeam(baseballTeam);
        return user;
    }

    @Transactional
    public User completeSignup(Long userId) {
        User user = userJpaRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        user.completeSignup();
        return user;
    }

    @Transactional
    public User completeSignupWithInfo(Long userId, User.GenderType gender, Integer age) {
        User user = userJpaRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        user.updateOptionalInfo(gender, age);
        user.completeSignup();
        return user;
    }
}
