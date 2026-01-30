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
            @ApiResponse(responseCode = "401", description = "소셜 인증 실패 (SOCIAL001, SOCIAL003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "정지/탈퇴 회원 (USER002, USER003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login/{provider}")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @PathVariable("provider") SocialProvider provider,
            @Valid @RequestBody SocialLoginRequest request) {
        LoginResult result = accountAppService.processSocialLogin(request.getToken(), provider);
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }

    @Operation(summary = "닉네임 중복 체크")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (VALIDATION001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/nickname/duplicate-check")
    public ResponseEntity<DuplicateResponse> checkNicknameDuplicate(
            @RequestParam("nickname") @NotBlank String nickname) {
        boolean isDuplicate = accountAppService.isNameDuplicate(nickname);
        return ResponseEntity.ok(DuplicateResponse.of(isDuplicate));
    }

    @Operation(summary = "이메일 중복 체크")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (VALIDATION001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/email/duplicate-check")
    public ResponseEntity<DuplicateResponse> checkEmailDuplicate(
            @RequestParam("email") @NotBlank String email) {
        boolean isDuplicate = accountAppService.isEmailDuplicate(email);
        return ResponseEntity.ok(DuplicateResponse.of(isDuplicate));
    }

    @Operation(summary = "액세스 토큰 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "갱신 성공"),
            @ApiResponse(responseCode = "401", description = "토큰 만료/무효 (JWT001, JWT002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = TokenResponse.from(accountAppService.refreshTokens(request.getRefreshToken()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "토큰 만료/무효 (JWT001, JWT002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "400", description = "필수 약관 미동의 또는 잘못된 단계 (USER007, USER008)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup/consent")
    public ResponseEntity<SignupStepResponse> processConsent(@Valid @RequestBody ConsentRequest request) {
        SignupStepResult result = accountAppService.processConsent(
                request.getUserId(),
                request.getPersonalInfoRequired(),
                request.getAgreeMarketing()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @Operation(summary = "회원가입 - 프로필 설정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 단계 또는 유효성 오류 (USER008, USER009)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "닉네임/이메일 중복 (USER004, USER006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup/profile")
    public ResponseEntity<SignupStepResponse> processProfile(@Valid @RequestBody ProfileRequest request) {
        SignupStepResult result = accountAppService.processProfile(
                request.getUserId(),
                request.getEmail(),
                request.getNickname()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @Operation(summary = "회원가입 - 팀 선택")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 단계 (USER008)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 또는 팀 없음 (USER001, TEAM001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup/team")
    public ResponseEntity<SignupStepResponse> processTeamSelection(@Valid @RequestBody TeamSelectionRequest request) {
        SignupStepResult result = accountAppService.processTeamSelection(
                request.getUserId(),
                request.getTeamCode()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @Operation(summary = "회원가입 완료 (추가 정보 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 단계 (USER008)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup/complete")
    public ResponseEntity<SignupCompleteResponse> completeSignup(@Valid @RequestBody SignupSkipCompleteRequest request) {
        LoginResult result = accountAppService.completeSignup(request.getUserId());
        return ResponseEntity.ok(SignupCompleteResponse.from(result));
    }

    @Operation(summary = "회원가입 완료 (추가 정보 포함)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 단계 (USER008)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup/complete-with-info")
    public ResponseEntity<SignupCompleteResponse> completeSignupWithInfo(@Valid @RequestBody SignupCompleteWithInfoRequest request) {
        LoginResult result = accountAppService.completeSignupWithInfo(
                request.getUserId(),
                request.getGender(),
                request.getAge()
        );
        return ResponseEntity.ok(SignupCompleteResponse.from(result));
    }
}
