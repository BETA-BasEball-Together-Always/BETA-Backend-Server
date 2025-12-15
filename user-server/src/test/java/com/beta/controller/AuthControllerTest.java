package com.beta.controller;

import com.beta.account.application.AccountAppService;
import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.TokenDto;
import com.beta.account.application.dto.UserDto;
import com.beta.support.RestDocsTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController REST Docs 테스트")
class AuthControllerTest extends RestDocsTestSupport {

    @MockBean
    private AccountAppService accountAppService;

    @Test
    @DisplayName("POST /api/v1/auth/login/{provider} - 소셜 로그인")
    void socialLogin() throws Exception {
        // given
        String requestBody = """
                {
                    "token": "kakao_access_token_12345"
                }
                """;

        UserDto userDto = UserDto.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        LoginResult loginResult = LoginResult.builder()
                .isNewUser(false)
                .accessToken("jwt_access_token")
                .refreshToken("jwt_refresh_token")
                .userInfo(userDto)
                .build();

        when(accountAppService.processSocialLogin(anyString(), any(SocialProvider.class)))
                .thenReturn(loginResult);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/{provider}", "KAKAO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("auth/social-login",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("provider").description("소셜 로그인 제공자 (KAKAO, NAVER, GOOGLE)")
                        ),
                        requestFields(
                                fieldWithPath("token").type(JsonFieldType.STRING)
                                        .description("소셜 로그인 제공자로부터 받은 액세스 토큰")
                        ),
                        responseFields(
                                fieldWithPath("newUser").type(JsonFieldType.BOOLEAN)
                                        .description("신규 사용자 여부 (true: 회원가입 필요, false: 로그인 완료)"),
                                fieldWithPath("userResponse").type(JsonFieldType.OBJECT)
                                        .description("사용자 응답 정보 (신규/기존 사용자에 따라 다름)"),
                                fieldWithPath("userResponse.accessToken").type(JsonFieldType.STRING)
                                        .description("JWT 액세스 토큰 (기존 사용자)").optional(),
                                fieldWithPath("userResponse.refreshToken").type(JsonFieldType.STRING)
                                        .description("JWT 리프레시 토큰 (기존 사용자)").optional(),
                                fieldWithPath("userResponse.user").type(JsonFieldType.OBJECT)
                                        .description("사용자 정보 (기존 사용자)").optional(),
                                fieldWithPath("userResponse.user.id").type(JsonFieldType.NUMBER)
                                        .description("사용자 ID").optional(),
                                fieldWithPath("userResponse.user.email").type(JsonFieldType.STRING)
                                        .description("사용자 이메일").optional(),
                                fieldWithPath("userResponse.user.password").type(JsonFieldType.STRING)
                                        .description("비밀번호").optional(),
                                fieldWithPath("userResponse.user.socialId").type(JsonFieldType.STRING)
                                        .description("소셜 ID").optional(),
                                fieldWithPath("userResponse.user.nickname").type(JsonFieldType.STRING)
                                        .description("닉네임").optional(),
                                fieldWithPath("userResponse.user.socialProvider").type(JsonFieldType.STRING)
                                        .description("소셜 제공자").optional(),
                                fieldWithPath("userResponse.user.favoriteTeamCode").type(JsonFieldType.STRING)
                                        .description("선호 팀 코드").optional(),
                                fieldWithPath("userResponse.user.favoriteTeamName").type(JsonFieldType.STRING)
                                        .description("선호 팀 이름").optional(),
                                fieldWithPath("userResponse.user.role").type(JsonFieldType.STRING)
                                        .description("사용자 역할").optional(),
                                fieldWithPath("userResponse.user.gender").type(JsonFieldType.STRING)
                                        .description("성별").optional(),
                                fieldWithPath("userResponse.user.age").type(JsonFieldType.NUMBER)
                                        .description("나이").optional(),
                                fieldWithPath("userResponse.user.bio").type(JsonFieldType.STRING)
                                        .description("자기소개").optional(),
                                fieldWithPath("userResponse.social").type(JsonFieldType.STRING)
                                        .description("소셜 정보 (신규 사용자)").optional(),
                                fieldWithPath("userResponse.teamList").type(JsonFieldType.ARRAY)
                                        .description("팀 목록 (신규 사용자)").optional(),
                                fieldWithPath("userResponse.teamList[].id").type(JsonFieldType.NUMBER)
                                        .description("팀 ID").optional(),
                                fieldWithPath("userResponse.teamList[].name").type(JsonFieldType.STRING)
                                        .description("팀 이름").optional(),
                                fieldWithPath("userResponse.teamList[].logoUrl").type(JsonFieldType.STRING)
                                        .description("팀 로고 URL").optional()
                        )
                ));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login/email - 이메일 로그인")
    void emailLogin() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Password123!"
                }
                """;

        UserDto userDto = UserDto.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        LoginResult loginResult = LoginResult.builder()
                .isNewUser(false)
                .accessToken("jwt_access_token")
                .refreshToken("jwt_refresh_token")
                .userInfo(userDto)
                .build();

        when(accountAppService.processEmailLogin(anyString(), anyString()))
                .thenReturn(loginResult);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("auth/email-login",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("newUser").type(JsonFieldType.BOOLEAN)
                                        .description("신규 사용자 여부 (false: 로그인 완료)"),
                                fieldWithPath("userResponse").type(JsonFieldType.OBJECT)
                                        .description("사용자 응답 정보"),
                                fieldWithPath("userResponse.accessToken").type(JsonFieldType.STRING)
                                        .description("JWT 액세스 토큰"),
                                fieldWithPath("userResponse.refreshToken").type(JsonFieldType.STRING)
                                        .description("JWT 리프레시 토큰"),
                                fieldWithPath("userResponse.user").type(JsonFieldType.OBJECT)
                                        .description("사용자 정보"),
                                fieldWithPath("userResponse.user.id").type(JsonFieldType.NUMBER)
                                        .description("사용자 ID"),
                                fieldWithPath("userResponse.user.email").type(JsonFieldType.STRING)
                                        .description("사용자 이메일").optional(),
                                fieldWithPath("userResponse.user.password").type(JsonFieldType.STRING)
                                        .description("비밀번호").optional(),
                                fieldWithPath("userResponse.user.socialId").type(JsonFieldType.STRING)
                                        .description("소셜 ID").optional(),
                                fieldWithPath("userResponse.user.nickname").type(JsonFieldType.STRING)
                                        .description("닉네임").optional(),
                                fieldWithPath("userResponse.user.socialProvider").type(JsonFieldType.STRING)
                                        .description("소셜 제공자").optional(),
                                fieldWithPath("userResponse.user.favoriteTeamCode").type(JsonFieldType.STRING)
                                        .description("선호 팀 코드").optional(),
                                fieldWithPath("userResponse.user.favoriteTeamName").type(JsonFieldType.STRING)
                                        .description("선호 팀 이름").optional(),
                                fieldWithPath("userResponse.user.role").type(JsonFieldType.STRING)
                                        .description("사용자 역할").optional(),
                                fieldWithPath("userResponse.user.gender").type(JsonFieldType.STRING)
                                        .description("성별").optional(),
                                fieldWithPath("userResponse.user.age").type(JsonFieldType.NUMBER)
                                        .description("나이").optional(),
                                fieldWithPath("userResponse.user.bio").type(JsonFieldType.STRING)
                                        .description("자기소개").optional()
                        )
                ));
    }

    @Test
    @DisplayName("GET /api/v1/auth/nickname/duplicate-check - 닉네임 중복 확인")
    void checkNicknameDuplicate() throws Exception {
        // given
        String nickname = "testuser";
        when(accountAppService.isNameDuplicate(nickname)).thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/v1/auth/nickname/duplicate-check")
                        .param("nickname", nickname))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(false))
                .andDo(document("auth/check-nickname",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("duplicate").type(JsonFieldType.BOOLEAN)
                                        .description("중복 여부 (true: 중복됨, false: 사용 가능)")
                        )
                ));
    }

    @Test
    @DisplayName("GET /api/v1/auth/email/duplicate-check - 이메일 중복 확인")
    void checkEmailDuplicate() throws Exception {
        // given
        String email = "test@example.com";
        when(accountAppService.isEmailDuplicate(email)).thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/v1/auth/email/duplicate-check")
                        .param("email", email))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(false))
                .andDo(document("auth/check-email",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("duplicate").type(JsonFieldType.BOOLEAN)
                                        .description("중복 여부 (true: 중복됨, false: 사용 가능)")
                        )
                ));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - 액세스 토큰 갱신")
    void refreshAccessToken() throws Exception {
        // given
        String requestBody = """
                {
                    "refreshToken": "jwt_refresh_token_12345"
                }
                """;

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("new_jwt_access_token")
                .refreshToken("new_jwt_refresh_token")
                .build();

        when(accountAppService.refreshTokens(anyString()))
                .thenReturn(tokenDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("auth/refresh-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                                        .description("리프레시 토큰")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").type(JsonFieldType.STRING)
                                        .description("새로 발급된 액세스 토큰"),
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                                        .description("새로 발급된 리프레시 토큰")
                        )
                ));
    }
}
