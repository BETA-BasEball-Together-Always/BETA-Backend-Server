package com.beta.controller.account;

import com.beta.account.application.admin.AdminAuthFacadeService;
import com.beta.account.application.admin.dto.AdminLoginResult;
import com.beta.account.application.admin.dto.AdminTokenResult;
import com.beta.controller.account.request.AdminLoginRequest;
import com.beta.controller.account.response.AdminLoginResponse;
import com.beta.controller.account.response.AdminLogoutResponse;
import com.beta.controller.account.response.AdminMeResponse;
import com.beta.controller.account.response.AdminTokenResponse;
import com.beta.core.exception.account.InvalidTokenException;
import com.beta.core.response.ErrorResponse;
import com.beta.core.security.AdminAuthConstants;
import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Auth", description = "관리자 인증 관련 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthFacadeService adminAuthFacadeService;

    @Operation(summary = "관리자 카카오 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminLoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "401", description = "소셜 인증 실패 / JWT 인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "소셜 토큰 무효", value = """
                                            {"code": "SOCIAL001", "message": "유효하지 않은 소셜 로그인 토큰입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "JWT 토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "JWT 토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "JWT 인증 처리 오류", value = """
                                            {"code": "JWT003", "message": "토큰 처리 중 오류가 발생했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음 / 탈퇴 / 정지 계정",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "관리자 권한 없음", value = """
                                            {"code": "ADMIN001", "message": "관리자 권한의 사용자가 아닙니다.", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "탈퇴한 사용자", value = """
                                            {"code": "USER002", "message": "탈퇴한 사용자입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "정지된 사용자", value = """
                                            {"code": "USER003", "message": "정지된 사용자입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "500", description = "소셜 API 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "SOCIAL002", "message": "소셜 로그인 API 호출 중 오류가 발생했습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/auth/login/kakao")
    public ResponseEntity<AdminLoginResponse> loginWithKakao(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResult result = adminAuthFacadeService.loginWithKakao(request.token());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(AdminLoginResponse.from(result));
    }

    @Operation(summary = "관리자 액세스 토큰 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminTokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 누락/무효 / JWT 인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "리프레시 토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "JWT 토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "JWT 인증 처리 오류", value = """
                                            {"code": "JWT003", "message": "토큰 처리 중 오류가 발생했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "403", description = "관리자 아님 / 탈퇴 / 정지 계정",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "관리자 권한 없음", value = """
                                            {"code": "ADMIN001", "message": "관리자 권한의 사용자가 아닙니다.", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "탈퇴한 사용자", value = """
                                            {"code": "USER002", "message": "탈퇴한 사용자입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "정지된 사용자", value = """
                                            {"code": "USER003", "message": "정지된 사용자입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/auth/refresh")
    public ResponseEntity<AdminTokenResponse> refreshTokens(
            @CookieValue(name = AdminAuthConstants.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("리프레시 토큰이 존재하지 않습니다.");
        }

        AdminTokenResult result = adminAuthFacadeService.refreshTokens(refreshToken);
        ResponseCookie rotatedRefreshTokenCookie = createRefreshTokenCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, rotatedRefreshTokenCookie.toString())
                .body(AdminTokenResponse.from(result));
    }

    @Operation(summary = "관리자 로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminLogoutResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 처리 오류", value = """
                                            {"code": "JWT003", "message": "토큰 처리 중 오류가 발생했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "ADMIN001", "message": "관리자 권한의 사용자가 아닙니다.", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/auth/logout")
    public ResponseEntity<AdminLogoutResponse> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        adminAuthFacadeService.logout(userDetails.userId());
        ResponseCookie clearCookie = clearRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(AdminLogoutResponse.ofSuccess());
    }

    @Operation(summary = "현재 관리자 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminMeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 처리 오류", value = """
                                            {"code": "JWT003", "message": "토큰 처리 중 오류가 발생했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "ADMIN001", "message": "관리자 권한의 사용자가 아닙니다.", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @GetMapping("/me")
    public ResponseEntity<AdminMeResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(AdminMeResponse.from(userDetails));
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(AdminAuthConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(AdminAuthConstants.REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(AdminAuthConstants.REFRESH_TOKEN_TTL)
                .build();
    }

    private ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(AdminAuthConstants.REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(AdminAuthConstants.REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
