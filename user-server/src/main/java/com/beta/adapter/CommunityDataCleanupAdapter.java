package com.beta.adapter;

import com.beta.account.application.port.CommunityDataCleanupPort;
import com.beta.community.infra.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CommunityDataCleanupAdapter implements CommunityDataCleanupPort {

    private final PostJpaRepository postJpaRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final EmotionJpaRepository emotionJpaRepository;
    private final CommentLikeJpaRepository commentLikeJpaRepository;
    private final PostImageJpaRepository postImageJpaRepository;
    private final UserBlockJpaRepository userBlockJpaRepository;

    @Override
    @Transactional
    public void deleteAllUserCommunityData(Long userId) {
        commentLikeJpaRepository.deleteAllByUserId(userId);
        emotionJpaRepository.deleteAllByUserId(userId);
        commentJpaRepository.deleteAllByUserId(userId);
        postImageJpaRepository.deleteAllByUserId(userId);
        postJpaRepository.deleteAllByUserId(userId);
        userBlockJpaRepository.deleteAllByBlockerIdOrBlockedId(userId);
    }
}
