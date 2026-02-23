package com.beta.core.port;

import com.beta.core.port.dto.MyPostInfo;

import java.util.List;

public interface CommunityPort {

    List<MyPostInfo> findMyPosts(Long userId, Long cursor, int pageSize);

    List<MyPostInfo> findCommentedPosts(Long userId, Long cursor, int pageSize);

    List<MyPostInfo> findLikedPosts(Long userId, Long cursor, int pageSize);

    List<MyPostInfo> findUserPostsWithChannelFilter(Long userId, List<String> channels, Long cursor, int pageSize);
}
