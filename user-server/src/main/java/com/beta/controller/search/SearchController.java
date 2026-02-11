package com.beta.controller.search;

import com.beta.controller.search.request.SearchHashtagRequest;
import com.beta.controller.search.request.SearchPostRequest;
import com.beta.controller.search.request.SearchSuggestionsRequest;
import com.beta.controller.search.request.SearchUserRequest;
import com.beta.controller.search.request.SearchChannel;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.controller.search.response.SearchHashtagResponse;
import com.beta.controller.search.response.SearchMyLogsResponse;
import com.beta.controller.search.response.SearchPostResponse;
import com.beta.controller.search.response.SearchSuggestionsResponse;
import com.beta.controller.search.response.SearchUserResponse;
import com.beta.core.response.ErrorResponse;
import jakarta.validation.Valid;
import com.beta.search.application.SearchAppService;
import com.beta.search.application.SearchFacadeService;
import com.beta.search.application.dto.SearchHashtagResult;
import com.beta.search.application.dto.SearchPostResult;
import com.beta.search.application.dto.SearchSuggestionsResult;
import com.beta.search.application.dto.SearchUserResult;
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
import com.beta.controller.community.response.MessageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Search", description = "검색 관련 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchAppService searchAppService;
    private final SearchFacadeService searchFacadeService;

    @Operation(
            summary = "내 최신 검색어 기록 조회",
            description = "로그인한 사용자의 최신 검색어 기록을 최대 5개까지 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (결과가 없는 경우 빈 배열([]) 반환)"),
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
    @GetMapping("/my-logs")
    public ResponseEntity<SearchMyLogsResponse> getMySearchLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(SearchMyLogsResponse.from(searchAppService.findMyRecentKeywords(userDetails.userId())));
    }

    @Operation(
            summary = "검색 기록 삭제",
            description = "로그인한 사용자의 특정 검색 기록을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
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
    @DeleteMapping("/my-logs/{logId}")
    public ResponseEntity<MessageResponse> deleteMySearchLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String logId
    ) {
        searchAppService.deleteMySearchLog(userDetails.userId(), logId);
        return ResponseEntity.ok(MessageResponse.of("검색 기록이 삭제되었습니다."));
    }

    @Operation(
            summary = "검색어 입력 중 추천",
            description = """
                            검색어 입력 시 자동완성을 위한 추천 검색어와 매칭되는 계정을 검색합니다.
                            onChange 이벤트에서 debounce와 함께 호출해주세요. (권장: 300ms)
                            추천 검색어는 최대 3개, 매칭 계정은 최대 10개까지 검색합니다.
                        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공 (결과가 없는 경우 빈 배열([]) 반환)"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "검색어 누락", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "keyword", "message": "검색어는 필수입니다", "rejectedValue": null}], "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "검색어 길이 초과", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "keyword", "message": "검색어는 30자 이하여야 합니다", "rejectedValue": "삽십자를초과하는아주긴검색어삽십자를초과하는아주긴검색어삽십자를초과하는아주긴검색어"}], "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @GetMapping("/suggestions")
    public ResponseEntity<SearchSuggestionsResponse> getSuggestions(
            @Valid @ModelAttribute SearchSuggestionsRequest request
    ) {
        SearchSuggestionsResult result = searchFacadeService.searchWhileTyping(request.keyword());
        return ResponseEntity.ok(SearchSuggestionsResponse.from(result));
    }

    @Operation(
            summary = "게시글 검색",
            description = """
                            키워드로 게시글을 검색합니다.
                            channel=ALL이면 전체 게시판, channel=TEAM이면 본인 팀 게시판에서 검색합니다.
                            검색 완료 시 검색 기록이 저장됩니다.
                        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공 (결과가 없는 경우 빈 배열([]) 반환)"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "검색어 누락", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "keyword", "message": "검색어는 필수입니다.", "rejectedValue": null}], "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "검색어 길이 초과", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "keyword", "message": "검색어는 30자 이하여야 합니다.", "rejectedValue": "삽십자를초과하는아주긴검색어삽십자를초과하는아주긴검색어삽십자를초과하는아주긴검색어"}], "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "채널 누락", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "channel", "message": "채널은 필수입니다.", "rejectedValue": null}], "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "채널 값 오류", value = """
                                            {"code": "COMMON002", "message": "잘못된 타입 값입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "커서 값 오류", value = """
                                            {"code": "SEARCH001", "message": "커서의 score, id는 둘다 존재하거나 둘다 비어있어야 합니다.", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "500", description = "검색 서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "검색 실패", value = """
                                    {"code": "SEARCH099", "message": "검색 중 오류가 발생했습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @GetMapping("/posts")
    public ResponseEntity<SearchPostResponse> searchPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute SearchPostRequest request
    ) {
        String targetChannel = request.channel() == SearchChannel.TEAM ? userDetails.teamCode() : SearchChannel.ALL.name();

        SearchCursor cursor = SearchCursor.of(request.cursorScore(), request.cursorId());
        SearchPostResult result = searchFacadeService.searchPosts(request.keyword(), targetChannel, userDetails.userId(), cursor);
        return ResponseEntity.ok(SearchPostResponse.from(result));
    }

    @Operation(
            summary = "계정 검색",
            description = """
                            키워드로 계정(사용자)을 검색합니다.
                            검색 완료 시 검색 기록이 저장됩니다.
                        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공 (결과가 없는 경우 빈 배열([]) 반환)"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "검색어 누락", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "keyword", "message": "검색어는 필수입니다.", "rejectedValue": null}], "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "검색어 길이 초과", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "keyword", "message": "검색어는 30자 이하여야 합니다.", "rejectedValue": "삽십자를초과하는아주긴검색어삽십자를초과하는아주긴검색어삽십자를초과하는아주긴검색어"}], "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "커서 값 오류", value = """
                                            {"code": "SEARCH001", "message": "커서의 score, id는 둘다 존재하거나 둘다 비어있어야 합니다.", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @GetMapping("/users")
    public ResponseEntity<SearchUserResponse> searchUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute SearchUserRequest request
    ) {
        SearchCursor cursor = SearchCursor.of(request.cursorScore(), request.cursorId());
        SearchUserResult result = searchFacadeService.searchUsers(request.keyword(), userDetails.userId(), cursor);
        return ResponseEntity.ok(SearchUserResponse.from(result));
    }

    @Operation(
            summary = "해시태그 검색",
            description = """
                            키워드로 해시태그를 검색합니다.
                            검색 완료 시 검색 기록이 저장됩니다.
                        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공 (결과가 없는 경우 빈 배열([]) 반환)"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "검색어 누락", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "keyword", "message": "검색어는 필수입니다.", "rejectedValue": null}], "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "검색어 길이 초과", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "fieldErrors": [{"field": "keyword", "message": "검색어는 30자 이하여야 합니다.", "rejectedValue": "삽십자를초과하는아주긴검색어삽십자를초과하는아주긴검색어삽십자를초과하는아주긴검색어"}], "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "커서 값 오류", value = """
                                            {"code": "SEARCH001", "message": "커서의 score, id는 둘다 존재하거나 둘다 비어있어야 합니다.", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @GetMapping("/hashtags")
    public ResponseEntity<SearchHashtagResponse> searchHashtags(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute SearchHashtagRequest request
    ) {
        SearchCursor cursor = SearchCursor.of(request.cursorScore(), request.cursorId());
        SearchHashtagResult result = searchFacadeService.searchHashtags(request.keyword(), userDetails.userId(), cursor);
        return ResponseEntity.ok(SearchHashtagResponse.from(result));
    }
}
