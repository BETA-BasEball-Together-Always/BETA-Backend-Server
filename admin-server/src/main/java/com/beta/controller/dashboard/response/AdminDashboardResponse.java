package com.beta.controller.dashboard.response;

import com.beta.application.dashboard.dto.AdminDashboardResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자 대시보드 응답")
public record AdminDashboardResponse(
        @Schema(description = "총 회원수(정상 상태)", example = "12456")
        Long totalMemberCount,
        @Schema(description = "총 회원수 증감(+/-)", example = "234")
        Long totalMemberDelta,
        @Schema(description = "오늘 게시물 수", example = "89")
        Long todayPostCount,
        @Schema(description = "오늘 게시물 수 증감(+/-)", example = "23")
        Long todayPostDelta,
        @Schema(description = "오늘 신규 가입자 수", example = "23")
        Long todayNewSignupCount,
        @Schema(description = "오늘 신규 가입자 수 증감(+/-)", example = "8")
        Long todayNewSignupDelta,
        @Schema(description = "처리 대기 신고 수 (미구현 TODO)", nullable = true, example = "5")
        Long pendingReportCount,
        @Schema(description = "실시간 피드 (최대 5개)")
        List<RealtimeFeedItem> realtimeFeeds,
        @Schema(description = "인기 토픽(해시태그)")
        List<PopularTopicItem> popularTopics
) {
    public static AdminDashboardResponse from(AdminDashboardResult result) {
        return new AdminDashboardResponse(
                result.totalMemberCount(),
                result.totalMemberDelta(),
                result.todayPostCount(),
                result.todayPostDelta(),
                result.todayNewSignupCount(),
                result.todayNewSignupDelta(),
                result.pendingReportCount(),
                result.realtimeFeeds().stream()
                        .map(RealtimeFeedItem::from)
                        .toList(),
                result.popularTopics().stream()
                        .map(PopularTopicItem::from)
                        .toList()
        );
    }


    @Schema(description = "실시간 피드 항목")
    public record RealtimeFeedItem(
            @Schema(description = "게시물 ID", example = "101")
            Long postId,
            @Schema(description = "작성자 닉네임", example = "siswe")
            String authorNickname,
            @Schema(description = "게시물 본문 미리보기", example = "오늘 경기 진짜...")
            String contentPreview,
            @Schema(description = "채널", example = "ALL")
            String channel,
            @Schema(description = "작성 시각", example = "2026-03-02T14:25:00")
            LocalDateTime createdAt,
            @Schema(description = "좋아요 수", example = "7")
            Integer likeCount,
            @Schema(description = "댓글 수", example = "1")
            Integer commentCount,
            @Schema(description = "게시물 썸네일 URL", nullable = true)
            String thumbnailUrl
    ) {
        public static RealtimeFeedItem from(AdminDashboardResult.RealtimeFeedItem item) {
            return new RealtimeFeedItem(
                    item.postId(),
                    item.authorNickname(),
                    item.contentPreview(),
                    item.channel(),
                    item.createdAt(),
                    item.likeCount(),
                    item.commentCount(),
                    item.thumbnailUrl()
            );
        }
    }

    @Schema(description = "인기 토픽 항목")
    public record PopularTopicItem(
            @Schema(description = "해시태그 ID", example = "31")
            Long hashtagId,
            @Schema(description = "해시태그명", example = "오늘의경기")
            String hashtag,
            @Schema(description = "사용 횟수", example = "234")
            Long usageCount
    ) {
        public static PopularTopicItem from(AdminDashboardResult.PopularTopicItem item) {
            return new PopularTopicItem(
                    item.hashtagId(),
                    item.hashtag(),
                    item.usageCount()
            );
        }
    }
}
