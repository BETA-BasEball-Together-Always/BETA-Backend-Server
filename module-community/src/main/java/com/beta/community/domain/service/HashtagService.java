package com.beta.community.domain.service;

import com.beta.community.domain.entity.Hashtag;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.PostHashtag;
import com.beta.community.infra.repository.HashtagJpaRepository;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagJpaRepository hashtagJpaRepository;
    private final PostHashtagJpaRepository postHashtagJpaRepository;

    @Transactional
    public List<String> processHashtags(Post post, List<String> hashtagNames) {
        if (hashtagNames == null || hashtagNames.isEmpty()) {
            return List.of();
        }

        List<String> validTagNames = hashtagNames.stream()
                .filter(tagName -> tagName != null && !tagName.isBlank())
                .distinct()
                .toList();

        if (validTagNames.isEmpty()) {
            return List.of();
        }

        validTagNames.forEach(hashtagJpaRepository::insertIgnore);
        hashtagJpaRepository.incrementUsageCountByTagNames(validTagNames);
        List<Hashtag> hashtags = hashtagJpaRepository.findByTagNameIn(validTagNames);

        List<PostHashtag> postHashtags = hashtags.stream()
                .map(hashtag -> PostHashtag.builder()
                        .post(post)
                        .hashtag(hashtag)
                        .build())
                .toList();

        postHashtagJpaRepository.saveAll(postHashtags);

        return validTagNames;
    }

    @Transactional
    public void decrementUsageCounts(List<String> hashtagNames) {
        if (hashtagNames == null || hashtagNames.isEmpty()) {
            return;
        }
        hashtagJpaRepository.decrementUsageCountByTagNames(hashtagNames);
    }
}
