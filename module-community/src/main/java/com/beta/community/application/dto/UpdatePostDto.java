package com.beta.community.application.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
public class UpdatePostDto {
    private String content;
    private List<String> hashtags;
    private List<Long> deletedImageIds;
    private List<MultipartFile> newImages;
}
