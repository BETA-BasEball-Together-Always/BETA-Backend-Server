package com.beta.adapter;

import com.beta.account.application.port.CommunityDataCleanupPort;
import com.beta.community.infra.repository.*;
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
        // 1. 사용자가 작성한 게시글 ID 조회
        List<Long> userPostIds = postJpaRepository.findAllIdsByUserId(userId);

        // 2. 사용자 게시글에 달린 모든 데이터 삭제 (다른 사용자가 남긴 것 포함)
        if (!userPostIds.isEmpty()) {
            // 2-1. 게시글의 댓글 ID 조회
            List<Long> commentIds = commentJpaRepository.findAllIdsByPostIdIn(userPostIds);

            // 2-2. 댓글에 달린 좋아요 삭제
            if (!commentIds.isEmpty()) {
                commentLikeJpaRepository.deleteAllByCommentIdIn(commentIds);
            }

            // 2-3. 게시글의 댓글 삭제
            commentJpaRepository.deleteAllByPostIdIn(userPostIds);

            // 2-4. 게시글의 감정표현 삭제
            emotionJpaRepository.deleteAllByPostIdIn(userPostIds);

            // 2-5. 게시글의 이미지 삭제
            postImageJpaRepository.deleteAllByPostIdIn(userPostIds);

            // 2-6. 게시글의 해시태그 연결 삭제
            postHashtagJpaRepository.deleteAllByPostIdIn(userPostIds);

            // 2-7. 게시글 삭제
            postJpaRepository.deleteAllByUserId(userId);
        }

        // 3. 사용자가 다른 게시글에 남긴 활동 삭제
        commentLikeJpaRepository.deleteAllByUserId(userId);
        emotionJpaRepository.deleteAllByUserId(userId);
        commentJpaRepository.deleteAllByUserId(userId);

        // 4. 차단 관계 삭제
        userBlockJpaRepository.deleteAllByBlockerIdOrBlockedId(userId);
    }
}
