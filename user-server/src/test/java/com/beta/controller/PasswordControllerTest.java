package com.beta.controller;

import com.beta.account.application.AccountAppService;
import com.beta.support.RestDocsTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PasswordController REST Docs 테스트")
class PasswordControllerTest extends RestDocsTestSupport {

    @MockitoBean
    private AccountAppService accountAppService;

    @Test
    @DisplayName("POST /api/v1/password/code - 비밀번호 재설정 인증코드 전송")
    void sendPasswordResetCode() throws Exception {
        // given
        String email = "test@example.com";
        when(accountAppService.sendPasswordResetCode(email)).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/v1/password/code")
                        .param("email", email))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증코드가 이메일로 전송되었습니다."))
                .andDo(document("password/send-code",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("요청 성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("POST /api/v1/password/verify - 인증코드 검증")
    void verifyPasswordResetCode() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "code": "123456"
                }
                """;
        when(accountAppService.verifyPasswordResetCode("test@example.com", "123456"))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/v1/password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증코드가 확인되었습니다."))
                .andDo(document("password/verify-code",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("6자리 인증코드 (예: 123456)")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("검증 성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("POST /api/v1/password/reset - 비밀번호 재설정")
    void resetPassword() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "code": "123456",
                    "newPassword": "NewPassword123!"
                }
                """;
        when(accountAppService.resetPassword("test@example.com", "123456", "NewPassword123!"))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/v1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."))
                .andDo(document("password/reset-password",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("6자리 인증코드"),
                                fieldWithPath("newPassword").type(JsonFieldType.STRING)
                                        .description("새 비밀번호 (8자 이상, 영문, 숫자, 특수문자 포함)")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("재설정 성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("POST /api/v1/password/code - 이메일 누락 시 500 에러")
    void sendPasswordResetCode_MissingEmail() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/password/code"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/v1/password/verify - 잘못된 이메일 형식 시 400 에러")
    void verifyPasswordResetCode_InvalidEmailFormat() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "invalid-email",
                    "code": "123456"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/password/verify - 잘못된 코드 형식 시 400 에러")
    void verifyPasswordResetCode_InvalidCodeFormat() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "code": "12345"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/password/reset - 약한 비밀번호 형식 시 400 에러")
    void resetPassword_WeakPasswordFormat() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "code": "123456",
                    "newPassword": "weak"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
