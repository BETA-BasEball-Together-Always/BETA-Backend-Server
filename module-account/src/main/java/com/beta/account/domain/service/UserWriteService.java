package com.beta.account.domain.service;

import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.entity.UserConsents;
import com.beta.account.infra.repository.UserConsentJpaRepository;
import com.beta.account.infra.repository.UserJpaRepository;
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

    public void saveAgreements(Boolean agreeMarketing, Boolean personalInfoRequired, Long id) {
        userConsentJpaRepository.save(UserConsents.builder()
                .userId(id)
                .agreeMarketing(agreeMarketing)
                .personalInfoRequired(personalInfoRequired)
                .build());
    }
}
