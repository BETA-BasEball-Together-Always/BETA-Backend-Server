package com.beta.account.application.dto;

import com.beta.account.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String password;
    private String socialId;
    private String nickname;
    private SocialProvider socialProvider;
    private String favoriteTeamCode;
    private String favoriteTeamName;
    private String role;
    private String gender;
    private Integer age;
    private String bio;

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .socialId(user.getSocialId())
                .nickname(user.getNickname())
                .socialProvider(user.getSocialProvider())
                .favoriteTeamCode(user.getBaseballTeam().getCode())
                .favoriteTeamName(user.getBaseballTeam().getTeamNameKr())
                .role(user.getRole().name())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .age(user.getAge())
                .bio(user.getBio())
                .build();
    }
}
