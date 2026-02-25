package com.beta.controller.home;

import com.beta.community.application.HomeAppService;
import com.beta.community.application.dto.HomeDto;
import com.beta.controller.home.response.HomeResponse;
import com.beta.core.response.ErrorResponse;
import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeAppService homeAppService;

    @Operation(summary = "홈 화면 조회", description = """
            홈 화면 데이터를 조회합니다.
            - KBO 팀 순위: 10개 팀 순위 (매일 00시, 06시 갱신)
            - 인기 게시글: ALL 채널, 최근 24시간 내 인기순 5개
            - 차단한 사용자의 게시글은 제외됩니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HomeResponse.class))),
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
    @GetMapping
    public ResponseEntity<HomeResponse> getHome(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        HomeDto homeDto = homeAppService.getHomeData(userDetails.userId());
        return ResponseEntity.ok(HomeResponse.from(homeDto));
    }
}
