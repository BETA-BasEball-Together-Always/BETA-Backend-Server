package com.beta.controller.account;

import com.beta.account.application.AccountAppService;
import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.SignupStepResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.controller.account.request.*;
import com.beta.controller.account.response.*;
import com.beta.core.response.ErrorResponse;
import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountAppService accountAppService;

    @Operation(summary = "소셜 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 이메일 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "유효성 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "소셜 이메일 없음", value = """
                                            {"code": "SOCIAL004", "message": "소셜 계정에 등록된 이메일이 없습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "401", description = "소셜 인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "소셜 토큰 무효", value = """
                                            {"code": "SOCIAL001", "message": "유효하지 않은 소셜 로그인 토큰입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "Apple IdToken 무효", value = """
                                            {"code": "SOCIAL003", "message": "유효하지 않은 Apple IdToken입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "403", description = "정지/탈퇴 회원",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "탈퇴한 사용자", value = """
                                            {"code": "USER002", "message": "탈퇴한 사용자입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "정지된 사용자", value = """
                                            {"code": "USER003", "message": "정지된 사용자입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "409", description = "이메일 중복 (다른 소셜 계정으로 가입됨)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER006", "message": "이미 KAKAO로 가입된 이메일입니다.", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "500", description = "소셜 로그인 API 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "SOCIAL002", "message": "소셜 로그인 API 호출 중 오류가 발생했습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/login/{provider}")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @PathVariable("provider") SocialProvider provider,
            @Valid @RequestBody SocialLoginRequest request) {
        LoginResult result = accountAppService.processSocialLogin(
                request.getToken(), provider, request.getDeviceId()
        );
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }

    @Operation(summary = "닉네임 중복 체크")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @GetMapping("/nickname/duplicate-check")
    public ResponseEntity<DuplicateResponse> checkNicknameDuplicate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("nickname") @NotBlank String nickname) {
        boolean isDuplicate = accountAppService.isNameDuplicate(nickname);
        return ResponseEntity.ok(DuplicateResponse.of(isDuplicate));
    }

    @Operation(summary = "액세스 토큰 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "갱신 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "401", description = "토큰 만료/무효",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = TokenResponse.from(accountAppService.refreshTokens(request.getRefreshToken()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) LogoutRequest request) {
        accountAppService.logout(userDetails.userId(), request.getDeviceId());
        return ResponseEntity.ok(LogoutResponse.success());
    }

    @Operation(summary = "회원가입 - 약관 동의")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "필수 약관 미동의 / 잘못된 단계 / 유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "필수 약관 미동의", value = """
                                            {"code": "USER007", "message": "개인정보 수집 및 이용에 대한 필수 동의가 필요합니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "잘못된 단계", value = """
                                            {"code": "USER008", "message": "잘못된 회원가입 단계입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "입력값 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/signup/consent")
    public ResponseEntity<SignupStepResponse> processConsent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ConsentRequest request) {
        SignupStepResult result = accountAppService.processConsent(
                userDetails.userId(),
                request.getPersonalInfoRequired(),
                request.getAgreeMarketing()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @Operation(summary = "회원가입 - 프로필 설정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 단계 / 닉네임 길이 오류 / 유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "잘못된 단계", value = """
                                            {"code": "USER008", "message": "잘못된 회원가입 단계입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "닉네임 길이 오류", value = """
                                            {"code": "USER009", "message": "닉네임은 2-13자 사이여야 합니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "입력값 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "409", description = "닉네임 중복",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "닉네임 중복", value = """
                                    {"code": "USER004", "message": "이미 존재하는 이름입니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/signup/profile")
    public ResponseEntity<SignupStepResponse> processProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileRequest request) {
        SignupStepResult result = accountAppService.processProfile(
                userDetails.userId(),
                request.getNickname()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @Operation(summary = "회원가입 - 팀 선택")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 단계 / 유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "잘못된 단계", value = """
                                            {"code": "USER008", "message": "잘못된 회원가입 단계입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "입력값 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "사용자 또는 팀 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "사용자 없음", value = """
                                            {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "팀 없음", value = """
                                            {"code": "TEAM001", "message": "해당 구단은 존재하지 않습니다.", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @PostMapping("/signup/team")
    public ResponseEntity<SignupStepResponse> processTeamSelection(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TeamSelectionRequest request) {
        SignupStepResult result = accountAppService.processTeamSelection(
                userDetails.userId(),
                request.getTeamCode()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @Operation(summary = "회원가입 완료 (추가 정보 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 단계",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER008", "message": "잘못된 회원가입 단계입니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/signup/complete")
    public ResponseEntity<SignupCompleteResponse> completeSignup(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LoginResult result = accountAppService.completeSignup(userDetails.userId());
        return ResponseEntity.ok(SignupCompleteResponse.from(result));
    }

    @Operation(summary = "회원가입 완료 (추가 정보 포함)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 단계 / 유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "잘못된 단계", value = """
                                            {"code": "USER008", "message": "잘못된 회원가입 단계입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "입력값 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/signup/complete-with-info")
    public ResponseEntity<SignupCompleteResponse> completeSignupWithInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SignupCompleteWithInfoRequest request) {
        LoginResult result = accountAppService.completeSignupWithInfo(
                userDetails.userId(),
                request.getGender(),
                request.getAge()
        );
        return ResponseEntity.ok(SignupCompleteResponse.from(result));
    }
}
