package com.beta.community.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageInfo {

    private Long imageId;
    private String imageUrl;

    public static ImageInfo of(Long imageId, String imageUrl) {
        return ImageInfo.builder()
                .imageId(imageId)
                .imageUrl(imageUrl)
                .build();
    }
}
