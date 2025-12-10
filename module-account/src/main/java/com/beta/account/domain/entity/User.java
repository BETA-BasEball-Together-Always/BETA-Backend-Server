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

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "favorite_team_code", referencedColumnName = "code", nullable = false)
    private BaseballTeam baseballTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private GenderType gender;

    @Column(name = "age")
    private Integer age;

    @Column(name = "bio", length = 50)
    private String bio;

    @Builder
    public User(String socialId, String email, String password, String nickname, SocialProvider socialProvider, UserStatus status, UserRole role, BaseballTeam baseballTeam, GenderType gender, Integer age, String bio) {
        this.socialId = socialId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.socialProvider = socialProvider;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : UserRole.USER;
        this.baseballTeam = baseballTeam;
        this.gender = gender;
        this.age = age;
        this.bio = bio;
    }

    public static User createNewUser(UserDto userDto, String password, BaseballTeam baseballTeam, String socialId, SocialProvider socialProvider) {
        return User.builder().socialId(socialId)
                .email(userDto.getEmail())
                .password(password)
                .nickname(userDto.getNickname())
                .socialProvider(socialProvider)
                .baseballTeam(baseballTeam)
                .gender(userDto.getGender() != null ? GenderType.valueOf(userDto.getGender()) : null)
                .age(userDto.getAge())
                .bio(userDto.getBio())
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

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
