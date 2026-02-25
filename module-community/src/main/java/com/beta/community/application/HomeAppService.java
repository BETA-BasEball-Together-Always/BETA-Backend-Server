package com.beta.community.application;

import com.beta.community.application.dto.HomeDto;
import com.beta.community.application.dto.ImageInfo;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.PostHashtag;
import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import com.beta.community.domain.service.UserBlockReadService;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.repository.PostQueryRepository;
import com.beta.core.infra.client.kbo.KboRankingClient;
import com.beta.core.infra.client.kbo.TeamRanking;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeAppService {

    private static final int POPULAR_POSTS_LIMIT = 5;
    private static final int POPULAR_POSTS_HOURS = 24;

    private final KboRankingClient kboRankingClient;
    private final PostQueryRepository postQueryRepository;
    private final PostImageJpaRepository postImageJpaRepository;
    private final PostHashtagJpaRepository postHashtagJpaRepository;
    private final UserBlockReadService userBlockReadService;
    private final UserPort userPort;

    @Transactional(readOnly = true)
    public HomeDto getHomeData(Long userId) {
        List<TeamRanking> kboRankings = kboRankingClient.getRankings();
        List<HomeDto.TeamRankingDto> teamRankingDtos = kboRankings.stream()
                .map(HomeDto.TeamRankingDto::from)
                .toList();

        List<Long> blockedUserIds = userBlockReadService.findBlockedUserIds(userId);
        List<Post> popularPosts = postQueryRepository.findPopularPostsWithinHours(
                POPULAR_POSTS_HOURS,
                POPULAR_POSTS_LIMIT,
                blockedUserIds
        );

        List<Long> postIds = popularPosts.stream().map(Post::getId).toList();
        List<Long> userIds = popularPosts.stream().map(Post::getUserId).distinct().toList();

        Map<Long, List<ImageInfo>> imagesMap = getImagesMap(postIds);
        Map<Long, List<String>> hashtagsMap = getHashtagsMap(postIds);
        Map<Long, AuthorInfo> authorMap = userPort.findAuthorsByIds(userIds);

        List<HomeDto.PopularPostDto> popularPostDtos = popularPosts.stream()
                .map(post -> HomeDto.PopularPostDto.from(post, imagesMap, hashtagsMap, authorMap))
                .toList();

        return HomeDto.builder()
                .teamRankings(teamRankingDtos)
                .popularPosts(popularPostDtos)
                .build();
    }

    private Map<Long, List<ImageInfo>> getImagesMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        List<PostImage> images = postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(postIds, Status.ACTIVE);
        return images.stream()
                .collect(Collectors.groupingBy(
                        PostImage::getPostId,
                        Collectors.mapping(
                                img -> ImageInfo.of(img.getId(), img.getImgUrl()),
                                Collectors.toList()
                        )
                ));
    }

    private Map<Long, List<String>> getHashtagsMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        List<PostHashtag> postHashtags = postHashtagJpaRepository.findByPost_IdIn(postIds);
        return postHashtags.stream()
                .collect(Collectors.groupingBy(
                        ph -> ph.getPost().getId(),
                        Collectors.mapping(ph -> ph.getHashtag().getTagName(), Collectors.toList())
                ));
    }
}
