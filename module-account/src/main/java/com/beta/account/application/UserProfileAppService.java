package com.beta.account.application;

import com.beta.account.application.dto.UserProfilePostListDto;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.UserReadService;
import com.beta.core.exception.community.BlockedUserProfileException;
import com.beta.core.port.BlockPort;
import com.beta.core.port.CommunityPort;
import com.beta.core.port.dto.MyPostInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileAppService {

    private final UserReadService userReadService;
    private final CommunityPort communityPort;
    private final BlockPort blockPort;

    private static final int PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public UserProfilePostListDto getUserPosts(Long viewerId, String viewerTeamCode, Long targetUserId, Long cursor) {
        // 1. 대상 사용자 조회
        User targetUser = userReadService.findUserById(targetUserId);

        // 2. 차단 여부 확인
        if (blockPort.isBlocked(viewerId, targetUserId)) {
            throw new BlockedUserProfileException();
        }

        // 3. 채널 필터 결정
        String targetTeamCode = targetUser.getBaseballTeam() != null
                ? targetUser.getBaseballTeam().getCode()
                : null;
        List<String> allowedChannels = determineAllowedChannels(viewerTeamCode, targetTeamCode);

        // 4. 게시글 조회
        List<MyPostInfo> posts = communityPort.findUserPostsWithChannelFilter(
                targetUserId, allowedChannels, cursor, PAGE_SIZE);

        // 5. 페이징 처리
        boolean hasNext = posts.size() > PAGE_SIZE;
        if (hasNext) {
            posts = posts.subList(0, PAGE_SIZE);
        }

        Long nextCursor = (hasNext && !posts.isEmpty()) ? posts.getLast().getPostId() : null;

        // 6. DTO 변환
        List<UserProfilePostListDto.PostDto> postDtos = posts.stream()
                .map(UserProfilePostListDto.PostDto::from)
                .toList();

        return UserProfilePostListDto.of(targetUser, postDtos, hasNext, nextCursor);
    }

    private List<String> determineAllowedChannels(String viewerTeamCode, String targetTeamCode) {
        List<String> channels = new ArrayList<>();
        channels.add("ALL");

        // 같은 팀인 경우 팀 채널도 포함
        if (viewerTeamCode != null && viewerTeamCode.equals(targetTeamCode)) {
            channels.add(targetTeamCode);
        }

        return channels;
    }
}
