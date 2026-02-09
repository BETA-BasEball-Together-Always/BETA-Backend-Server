package com.beta.controller.community.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "게시글 수정 요청")
public class UpdatePostRequest {

    @Schema(description = "게시글 내용 (1~2000자)", example = "수정된 게시글 내용입니다", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "게시글 내용은 필수입니다")
    @Size(min = 1, max = 2000, message = "게시글은 1~2000자 사이여야 합니다")
    private String content;

    @Schema(description = "해시태그 목록 (최대 5개, 각 20자 이하)", example = "[\"야구\", \"수정\"]")
    @Size(max = 5, message = "해시태그는 최대 5개까지 가능합니다")
    private List<@Size(max = 20, message = "해시태그는 20자 이하여야 합니다") String> hashtags = new ArrayList<>();

    @Schema(description = "삭제할 이미지 ID 목록 (게시글 조회 응답의 imageId 사용)", example = "[1, 2]")
    private List<Long> deletedImageIds = new ArrayList<>();

    @Schema(description = "새로 추가할 이미지 파일 목록 (기존 이미지 + 신규 이미지 합계 최대 5개)")
    @Size(max = 5, message = "이미지는 최대 5개까지 업로드 가능합니다")
    private List<MultipartFile> newImages = new ArrayList<>();
}
