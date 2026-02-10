package com.beta.core.port;

import com.beta.core.port.dto.PostInfo;

import java.util.List;
import java.util.Map;

public interface PostPort {
    Map<Long, PostInfo> findPostsByIds(List<Long> postIds, Long userId);
}
