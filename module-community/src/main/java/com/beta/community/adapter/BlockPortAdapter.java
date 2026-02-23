package com.beta.community.adapter;

import com.beta.community.domain.service.UserBlockReadService;
import com.beta.core.port.BlockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlockPortAdapter implements BlockPort {

    private final UserBlockReadService userBlockReadService;

    @Override
    public boolean isBlocked(Long blockerId, Long blockedId) {
        return userBlockReadService.isBlocked(blockerId, blockedId);
    }
}
