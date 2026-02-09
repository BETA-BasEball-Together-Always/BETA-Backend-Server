package com.beta.controller.community.request;

import com.beta.community.application.dto.CreatePostDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "게시글 작성 요청")
public class CreatePostRequest {

    @Schema(description = "게시글 내용 (1~2000자)", example = "오늘 경기 너무 좋았다!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "게시글 내용은 필수입니다")
    @Size(min = 1, max = 2000, message = "게시글은 1~2000자 사이여야 합니다")
    private String content;

    @Schema(description = "채널 구분 (TEAM: 내 팀 채널, ALL: 전체 채널)", example = "TEAM", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "채널 구분은 필수입니다")
    @Pattern(regexp = "^(TEAM|ALL)$", message = "채널은 TEAM 또는 ALL만 가능합니다")
    private String channel;

    @Schema(description = "해시태그 목록 (최대 5개, 각 20자 이하)", example = "[\"야구\", \"두산\"]")
    @Size(max = 5, message = "해시태그는 최대 5개까지 가능합니다")
    private List<@NotBlank @Size(max = 20, message = "해시태그는 20자 이하여야 합니다") String> hashtags = new ArrayList<>();

    @Schema(description = "이미지 파일 목록 (최대 5개, JPG/PNG/GIF/WEBP, 각 10MB 이하)")
    @Size(max = 5, message = "이미지는 최대 5개까지 업로드 가능합니다")
    private List<MultipartFile> images = new ArrayList<>();

    public CreatePostDto toDto() {
        return CreatePostDto.builder()
                .content(this.content)
                .channel(this.channel)
                .hashtags(this.hashtags)
                .images(this.images)
                .build();
    }
}
