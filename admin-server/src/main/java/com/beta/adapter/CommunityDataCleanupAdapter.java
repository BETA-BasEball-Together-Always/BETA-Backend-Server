package com.beta.adapter;

import com.beta.account.application.port.CommunityDataCleanupPort;
import com.beta.community.infra.repository.CommentJpaRepository;
import com.beta.community.infra.repository.CommentLikeJpaRepository;
import com.beta.community.infra.repository.EmotionJpaRepository;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.community.infra.repository.UserBlockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommunityDataCleanupAdapter implements CommunityDataCleanupPort {

    private final PostJpaRepository postJpaRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final EmotionJpaRepository emotionJpaRepository;
    private final CommentLikeJpaRepository commentLikeJpaRepository;
    private final PostImageJpaRepository postImageJpaRepository;
    private final PostHashtagJpaRepository postHashtagJpaRepository;
    private final UserBlockJpaRepository userBlockJpaRepository;

    @Override
    @Transactional
    public void deleteAllUserCommunityData(Long userId) {
        List<Long> userPostIds = postJpaRepository.findAllIdsByUserId(userId);

        if (!userPostIds.isEmpty()) {
            List<Long> commentIds = commentJpaRepository.findAllIdsByPostIdIn(userPostIds);

            if (!commentIds.isEmpty()) {
                commentLikeJpaRepository.deleteAllByCommentIdIn(commentIds);
            }

            commentJpaRepository.deleteAllByPostIdIn(userPostIds);
            emotionJpaRepository.deleteAllByPostIdIn(userPostIds);
            postImageJpaRepository.deleteAllByPostIdIn(userPostIds);
            postHashtagJpaRepository.deleteAllByPostIdIn(userPostIds);
            postJpaRepository.deleteAllByUserId(userId);
        }

        commentLikeJpaRepository.deleteAllByUserId(userId);
        emotionJpaRepository.deleteAllByUserId(userId);
        commentJpaRepository.deleteAllByUserId(userId);
        userBlockJpaRepository.deleteAllByBlockerIdOrBlockedId(userId);
    }
}
