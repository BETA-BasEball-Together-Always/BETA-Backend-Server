package com.beta.account.domain.entity;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.UserDto;
import com.beta.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_step", nullable = false, length = 30)
    private SignupStep signupStep;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "favorite_team_code", referencedColumnName = "code")
    private BaseballTeam baseballTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private GenderType gender;

    @Column(name = "age")
    private Integer age;

    @Column(name = "bio", length = 50)
    private String bio;

    @Column(name = "withdrawn_at")
    private java.time.LocalDateTime withdrawnAt;

    @Builder
    public User(String socialId, String email, String nickname, SocialProvider socialProvider,
                UserStatus status, UserRole role, SignupStep signupStep, BaseballTeam baseballTeam,
                GenderType gender, Integer age, String bio) {
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.socialProvider = socialProvider;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : UserRole.USER;
        this.signupStep = signupStep != null ? signupStep : SignupStep.COMPLETED;
        this.baseballTeam = baseballTeam;
        this.gender = gender;
        this.age = age;
        this.bio = bio;
    }

    public static User createNewSocialUser(String socialId, SocialProvider socialProvider, String email) {
        return User.builder()
                .socialId(socialId)
                .socialProvider(socialProvider)
                .email(email)
                .signupStep(SignupStep.SOCIAL_AUTHENTICATED)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }

    public enum UserStatus {
        ACTIVE,     // 정상 사용
        SUSPENDED,  // 정지
        WITHDRAWN   // 탈퇴
    }

    public enum UserRole {
        USER, ADMIN
    }

    public enum GenderType {
        M, F;
    }

    public void agreeConsent() {
        this.signupStep = SignupStep.CONSENT_AGREED;
    }

    public void updateProfile(String nickname) {
        this.nickname = nickname;
        this.signupStep = SignupStep.PROFILE_COMPLETED;
    }

    public void updateTeam(BaseballTeam baseballTeam) {
        this.baseballTeam = baseballTeam;
        this.signupStep = SignupStep.TEAM_SELECTED;
    }

    public void updateOptionalInfo(GenderType gender, Integer age) {
        this.gender = gender;
        this.age = age;
    }

    public void completeSignup() {
        this.signupStep = SignupStep.COMPLETED;
    }

    public boolean isSignupCompleted() {
        return this.signupStep == SignupStep.COMPLETED;
    }

    public void updateBio(String bio) {
        this.bio = (bio == null || bio.isBlank()) ? null : bio;
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = java.time.LocalDateTime.now();
    }
}
