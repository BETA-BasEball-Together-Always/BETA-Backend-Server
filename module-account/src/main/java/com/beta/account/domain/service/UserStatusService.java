package com.beta.account.domain.service;

import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.core.exception.account.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final UserJpaRepository userJpaRepository;

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

    public void validateNameDuplicate(boolean nameDuplicate) {
        if(nameDuplicate){
            throw new NameDuplicateException("이미 존재하는 이름입니다.");
        }
    }

    public void isNameDuplicate(String nickName) {
        if(userJpaRepository.existsByNickname(nickName)){
            throw new NameDuplicateException("이미 존재하는 닉네임입니다");
        }
    }

    public void isEmailDuplicate(String email) {
        if(userJpaRepository.existsByEmail(email)){
            throw new EmailDuplicateException("이미 존재하는 이메일입니다");
        }
    }
}
