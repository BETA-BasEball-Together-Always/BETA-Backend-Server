package com.beta.controller.community.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class UpdatePostRequest {

    @NotBlank(message = "게시글 내용은 필수입니다")
    @Size(min = 1, max = 2000, message = "게시글은 1~2000자 사이여야 합니다")
    private String content;

    @Size(max = 5, message = "해시태그는 최대 5개까지 가능합니다")
    private List<@Size(max = 20, message = "해시태그는 20자 이하여야 합니다") String> hashtags = new ArrayList<>();
}
