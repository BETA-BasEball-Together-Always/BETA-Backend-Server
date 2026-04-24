package com.beta.adapter;

import com.beta.account.application.port.CommunityDataCleanupPort;
import com.beta.community.infra.repository.UserBlockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CommunityDataCleanupAdapter implements CommunityDataCleanupPort {

    private final UserBlockJpaRepository userBlockJpaRepository;

    @Override
    @Transactional
    public void deleteUserBlockRelationships(Long userId) {
        userBlockJpaRepository.deleteAllByBlockerIdOrBlockedId(userId);
    }
}
