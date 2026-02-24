package com.beta.controller.account;

import com.beta.account.application.AccountAppService;
import com.beta.controller.account.request.UpdateBioRequest;
import com.beta.controller.account.response.UpdateBioResponse;
import com.beta.controller.account.response.WithdrawResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AccountAppService accountAppService;

    @Operation(summary = "한줄 소개 수정", description = """
            사용자의 한줄 소개를 수정합니다.
            - 빈 문자열("")이나 null이면 bio를 삭제합니다.
            - 일반 문자열이면 해당 값으로 업데이트합니다.
            - 최대 50자까지 입력 가능합니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UpdateBioResponse.class))),
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
                            })),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PatchMapping("/me/bio")
    public ResponseEntity<UpdateBioResponse> updateBio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateBioRequest request) {
        String updatedBio = accountAppService.updateBio(userDetails.userId(), request.getBio());
        return ResponseEntity.ok(UpdateBioResponse.of(updatedBio));
    }

    @Operation(summary = "계정 탈퇴", description = """
            계정 탈퇴를 요청합니다.
            - 탈퇴 요청 즉시 로그아웃 처리됩니다.
            - 30일 후 모든 데이터가 영구 삭제됩니다.
            - 30일 이내 재로그인 시 탈퇴가 취소될 수 있습니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 요청 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WithdrawResponse.class))),
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
    @DeleteMapping("/me")
    public ResponseEntity<WithdrawResponse> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDateTime withdrawnAt = accountAppService.processWithdrawal(userDetails.userId());
        return ResponseEntity.ok(WithdrawResponse.of(withdrawnAt));
    }
}
