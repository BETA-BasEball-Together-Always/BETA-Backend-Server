package com.beta.community.application.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
public class CreatePostDto {
    private String content;
    private String channel;
    private List<String> hashtags;
    private List<MultipartFile> images;
}
