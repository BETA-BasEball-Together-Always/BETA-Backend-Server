package com.beta.core.port;

import com.beta.core.port.dto.AuthorInfo;

import java.util.List;
import java.util.Map;

public interface UserPort {

    Map<Long, AuthorInfo> findAuthorsByIds(List<Long> userIds);
}
