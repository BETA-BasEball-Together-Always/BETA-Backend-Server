package com.beta.community.application.admin;

import com.beta.community.application.admin.dto.AdminCommunityDashboardMetricsResult;
import com.beta.community.domain.entity.Hashtag;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.DashboardQueryRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
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
public class AdminCommunityDashboardFacadeService {

    private final DashboardQueryRepository communityDashboardQueryRepository;
    private final PostImageJpaRepository postImageJpaRepository;
    private final UserPort userPort;

    @Transactional(readOnly = true)
    public AdminCommunityDashboardMetricsResult getDashboardCommunityMetrics(
            int realtimeFeedLimit,
            int popularTopicLimit
    ) {
        DashboardQueryRepository.PostMetricsSnapshot postMetrics =
                communityDashboardQueryRepository.getDashboardPostMetricsSnapshot();

        List<Post> realtimeFeedPosts = communityDashboardQueryRepository.findRealtimeFeedPosts(realtimeFeedLimit);
        List<Long> postIds = realtimeFeedPosts.stream()
                .map(Post::getId)
                .toList();
        List<Long> userIds = realtimeFeedPosts.stream()
                .map(Post::getUserId)
                .distinct()
                .toList();
        Map<Long, AuthorInfo> authorMap = userPort.findAuthorsByIds(userIds);
        Map<Long, String> thumbnailUrlMap = getThumbnailUrlMap(postIds);

        List<Hashtag> hashtags = communityDashboardQueryRepository.findPopularTopics(popularTopicLimit);

        return AdminCommunityDashboardMetricsResult.from(
                postMetrics,
                realtimeFeedPosts,
                authorMap,
                thumbnailUrlMap,
                hashtags
        );
    }

    private Map<Long, String> getThumbnailUrlMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        List<PostImage> images = postImageJpaRepository.findByPostIdInAndStatusOrderByPostIdAscSortAsc(
                postIds,
                Status.ACTIVE
        );
        return images.stream()
                .collect(Collectors.toMap(
                        PostImage::getPostId,
                        PostImage::getImgUrl,
                        (existing, replacement) -> existing
                ));
    }
}
