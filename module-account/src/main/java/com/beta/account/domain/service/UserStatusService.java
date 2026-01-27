package com.beta.account.domain.service;

import com.beta.account.domain.entity.SignupStep;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.core.exception.account.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final UserJpaRepository userJpaRepository;

    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 13;

    public void validateUserStatus(User user) {
        if (user.getStatus() == User.UserStatus.WITHDRAWN) {
            throw new UserWithdrawnException("탈퇴한 사용자입니다.");
        }

        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new UserSuspendedException("정지된 사용자입니다. 관리자에게 문의 하세요.");
        }
    }

    public void validateAgreePersonalInfo(Boolean agreePersonalInfo) {
        if(agreePersonalInfo == null || !agreePersonalInfo){
            throw new PersonalInfoAgreementRequiredException("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.");
        }
    }

    public void validateNameDuplicate(String nickName) {
        if(userJpaRepository.existsByNickname(nickName)){
            throw new NameDuplicateException("이미 존재하는 닉네임입니다");
        }
    }

    public void validateEmailDuplicate(String email) {
        if(userJpaRepository.existsByEmail(email)){
            throw new EmailDuplicateException("이미 존재하는 이메일입니다");
        }
    }

    public boolean isNameDuplicate(String nickName) {
        return userJpaRepository.existsByNickname(nickName);
    }

    public boolean isEmailDuplicate(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    public void validateSignupStep(User user, SignupStep expectedStep) {
        if (user.getSignupStep() != expectedStep) {
            throw new InvalidSignupStepException(
                    String.format("현재 회원가입 단계(%s)에서 진행할 수 없는 요청입니다. 기대 단계: %s",
                            user.getSignupStep(), expectedStep));
        }
    }

    public void validateNicknameLength(String nickname) {
        if (nickname == null || nickname.length() < MIN_NICKNAME_LENGTH || nickname.length() > MAX_NICKNAME_LENGTH) {
            throw new NicknameLengthException(
                    String.format("닉네임은 %d~%d자 사이여야 합니다", MIN_NICKNAME_LENGTH, MAX_NICKNAME_LENGTH));
        }
    }
}
