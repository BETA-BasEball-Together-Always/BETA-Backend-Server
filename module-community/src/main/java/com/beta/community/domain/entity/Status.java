package com.beta.community.domain.entity;

public enum Status {
    PENDING,
    ACTIVE,      // 정상 공개
    DELETED,     // 삭제됨
    HIDDEN,      // 관리자에 의해 숨김
    REPORTED     // 신고되어 검토 중
}
