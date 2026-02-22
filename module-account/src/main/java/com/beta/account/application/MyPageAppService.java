package com.beta.account.application;

import com.beta.account.application.dto.MyPostListDto;
import com.beta.core.port.CommunityPort;
import com.beta.core.port.dto.MyPostInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageAppService {

    private final CommunityPort communityPort;

    private static final int PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public MyPostListDto getMyPosts(Long userId, Long cursor) {
        List<MyPostInfo> posts = communityPort.findMyPosts(userId, cursor, PAGE_SIZE);
        return buildResult(posts);
    }

    @Transactional(readOnly = true)
    public MyPostListDto getCommentedPosts(Long userId, Long cursor) {
        List<MyPostInfo> posts = communityPort.findCommentedPosts(userId, cursor, PAGE_SIZE);
        return buildResult(posts);
    }

    @Transactional(readOnly = true)
    public MyPostListDto getLikedPosts(Long userId, Long cursor) {
        List<MyPostInfo> posts = communityPort.findLikedPosts(userId, cursor, PAGE_SIZE);
        return buildResult(posts);
    }

    private MyPostListDto buildResult(List<MyPostInfo> posts) {
        boolean hasNext = posts.size() > PAGE_SIZE;
        if (hasNext) {
            posts = posts.subList(0, PAGE_SIZE);
        }

        Long nextCursor = (hasNext && !posts.isEmpty()) ? posts.getLast().getPostId() : null;

        List<MyPostListDto.MyPostDto> postDtos = posts.stream()
                .map(MyPostListDto.MyPostDto::from)
                .toList();

        return MyPostListDto.builder()
                .posts(postDtos)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
