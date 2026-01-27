package com.beta.account.domain.entity;

public enum SignupStep {
    SOCIAL_AUTHENTICATED,  // 소셜 인증 완료 (최초 INSERT)
    CONSENT_AGREED,        // 개인정보동의 완료
    PROFILE_COMPLETED,     // 닉네임/이메일 입력 완료
    TEAM_SELECTED,         // 응원구단 선택 완료
    COMPLETED              // 회원가입 완료
}
