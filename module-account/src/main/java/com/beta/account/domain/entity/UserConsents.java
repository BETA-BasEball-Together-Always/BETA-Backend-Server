package com.beta.account.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_consents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConsents extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "personal_info_required", nullable = false)
    private Boolean personalInfoRequired;

    @Column(name = "personal_info_required_at")
    private LocalDateTime personalInfoRequiredAt;

    @Column(name = "agree_marketing", nullable = false)
    private Boolean agreeMarketing;

    @Column(name = "agree_marketing_at")
    private LocalDateTime agreeMarketingAt;

    @Builder
    public UserConsents(Long userId, Boolean personalInfoRequired, Boolean agreeMarketing) {
        this.userId = userId;
        this.personalInfoRequired = personalInfoRequired;
        this.personalInfoRequiredAt = personalInfoRequired ? LocalDateTime.now() : null;
        this.agreeMarketing = agreeMarketing;
        this.agreeMarketingAt = agreeMarketing ? LocalDateTime.now() : null;
    }
}
