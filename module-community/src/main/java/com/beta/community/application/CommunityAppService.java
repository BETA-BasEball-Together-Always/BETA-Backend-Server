package com.beta.community.application;

import com.beta.community.application.dto.CreatePostDto;
import com.beta.community.application.dto.PostDto;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.service.*;
import com.beta.core.exception.community.ImageUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityAppService {

    private final PostWriteService postWriteService;
    private final PostImageService postImageService;
    private final HashtagService hashtagService;
    private final ChannelValidationService channelValidationService;

    public PostDto createPost(Long userId, String userTeamCode, CreatePostDto dto) {

        String targetChannel = channelValidationService.validateAndResolveChannel(
                dto.getChannel(),
                userTeamCode
        );

        postImageService.validateImages(dto.getImages());

        Post post = Post.builder()
                .userId(userId)
                .content(dto.getContent())
                .channel(targetChannel)
                .build();
        Post savedPost = postWriteService.save(post);

        List<String> hashtags = hashtagService.processHashtags(
                savedPost,
                dto.getHashtags()
        );

        List<String> imageUrls;
        try {
            imageUrls = postImageService.uploadAndSaveImages(
                    savedPost.getId(),
                    userId,
                    dto.getImages()
            );
            postWriteService.activate(savedPost);
        } catch (Exception e) {
            log.warn("이미지 업로드 실패 : userId={}", userId, e);
            compensatePostCreation(savedPost, hashtags);
            throw new ImageUploadException(
                    "이미지 업로드 중 오류가 발생했습니다.",
                    e
            );
        }

        return PostDto.builder()
                .postId(savedPost.getId())
                .userId(savedPost.getUserId())
                .content(savedPost.getContent())
                .channel(savedPost.getChannel().name())
                .imageUrls(imageUrls)
                .hashtags(hashtags)
                .status(savedPost.getStatus().name())
                .createdAt(savedPost.getCreatedAt())
                .build();
    }

    private void compensatePostCreation(Post post, List<String> hashtags) {
        try {
            postWriteService.delete(post);
            hashtagService.decrementUsageCounts(hashtags);
        } catch (Exception e) {
            log.error("보상 트랜잭션 실패: postId={}", post.getId(), e);
        }
    }
}
