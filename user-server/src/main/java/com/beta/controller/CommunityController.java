package com.beta.controller;

import com.beta.community.application.CommunityAppService;
import com.beta.community.application.dto.PostDto;
import com.beta.controller.request.CreatePostRequest;
import com.beta.controller.response.CreatePostResponse;
import com.beta.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityAppService communityAppService;

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreatePostResponse> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute CreatePostRequest request) {

        PostDto postDto = communityAppService.createPost(
                userDetails.userId(),
                userDetails.teamCode(),
                request.toDto()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(CreatePostResponse.from(postDto));
    }
}
