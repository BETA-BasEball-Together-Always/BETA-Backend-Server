package com.beta.controller.request;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.UserDto;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupCompleteRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;

    @NotBlank(message = "응원팀 코드는 필수입니다")
    private String favoriteTeamCode;

    private String socialToken;

    private String social;

    private Integer age;

    private String gender;

    @Size(max = 50, message = "한줄소개는 50자 이하여야 합니다")
    private String bio;

    @NotNull(message = "개인정보 수집 및 이용 동의는 필수입니다")
    @AssertTrue(message = "개인정보 수집 및 이용에 동의해야 합니다")
    private Boolean personalInfoRequired;

    private Boolean agreeMarketing;

    public UserDto toUserDto() {
        return UserDto.builder()
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .favoriteTeamCode(this.favoriteTeamCode)
                .age(this.age)
                .gender(this.gender)
                .bio(this.bio)
                .socialProvider(this.social != null ? SocialProvider.valueOf(this.social) : SocialProvider.EMAIL)
                .build();
    }
}
