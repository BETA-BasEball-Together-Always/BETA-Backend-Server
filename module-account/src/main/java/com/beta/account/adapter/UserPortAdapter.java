package com.beta.account.adapter;

import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.UserReadService;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {

    private final UserReadService userReadService;

    @Override
    public Map<Long, AuthorInfo> findAuthorsByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<User> users = userReadService.findUsersByIds(userIds);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        this::toAuthorInfo,
                        (existing, replacement) -> existing
                ));
    }

    private AuthorInfo toAuthorInfo(User user) {
        return AuthorInfo.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .teamCode(user.getBaseballTeam() != null ? user.getBaseballTeam().getCode() : null)
                .build();
    }
}
