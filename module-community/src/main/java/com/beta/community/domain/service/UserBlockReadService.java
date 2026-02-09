package com.beta.community.domain.service;

import com.beta.community.domain.entity.UserBlock;
import com.beta.community.infra.repository.UserBlockJpaRepository;
import com.beta.core.exception.community.BlockNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBlockReadService {

    private final UserBlockJpaRepository userBlockJpaRepository;

    public UserBlock findByBlockerAndBlocked(Long blockerId, Long blockedId) {
        return userBlockJpaRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .orElseThrow(BlockNotFoundException::new);
    }

    public boolean isBlocked(Long blockerId, Long blockedId) {
        return userBlockJpaRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    public List<Long> findBlockedUserIds(Long blockerId) {
        return userBlockJpaRepository.findAllByBlockerId(blockerId).stream()
                .map(UserBlock::getBlockedId)
                .toList();
    }
}
