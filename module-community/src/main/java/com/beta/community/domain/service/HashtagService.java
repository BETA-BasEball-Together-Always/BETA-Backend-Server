package com.beta.community.domain.service;

import com.beta.community.domain.entity.Hashtag;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.PostHashtag;
import com.beta.community.infra.repository.HashtagJpaRepository;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagJpaRepository hashtagJpaRepository;
    private final PostHashtagJpaRepository postHashtagJpaRepository;

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

    public List<String> updateHashtags(Post post, List<String> newHashtagNames) {
        List<PostHashtag> existingPostHashtags = postHashtagJpaRepository.findByPostId(post.getId());
        Set<String> existingTagNames = existingPostHashtags.stream()
                .map(ph -> ph.getHashtag().getTagName())
                .collect(Collectors.toSet());

        List<String> newValidNames = (newHashtagNames == null ? List.<String>of() : newHashtagNames).stream()
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .toList();
        Set<String> newTagNameSet = Set.copyOf(newValidNames);

        List<String> toRemove = existingTagNames.stream()
                .filter(t -> !newTagNameSet.contains(t))
                .toList();

        List<String> toAdd = newValidNames.stream()
                .filter(t -> !existingTagNames.contains(t))
                .toList();

        if (!toRemove.isEmpty()) {
            List<Hashtag> removeHashtags = hashtagJpaRepository.findByTagNameIn(toRemove);
            postHashtagJpaRepository.deleteAllByPostIdAndHashtagIn(post.getId(), removeHashtags);
            hashtagJpaRepository.decrementUsageCountByTagNames(toRemove);
        }

        if (!toAdd.isEmpty()) {
            toAdd.forEach(hashtagJpaRepository::insertIgnore);
            hashtagJpaRepository.incrementUsageCountByTagNames(toAdd);
            List<Hashtag> addHashtags = hashtagJpaRepository.findByTagNameIn(toAdd);

            List<PostHashtag> newPostHashtags = addHashtags.stream()
                    .map(hashtag -> PostHashtag.builder()
                            .post(post)
                            .hashtag(hashtag)
                            .build())
                    .toList();
            postHashtagJpaRepository.saveAll(newPostHashtags);
        }

        return newValidNames;
    }

    public List<String> findHashtagNamesByPostId(Long postId) {
        return postHashtagJpaRepository.findByPostId(postId).stream()
                .map(ph -> ph.getHashtag().getTagName())
                .toList();
    }

    public void decrementUsageCounts(List<String> hashtagNames) {
        if (hashtagNames == null || hashtagNames.isEmpty()) {
            return;
        }
        hashtagJpaRepository.decrementUsageCountByTagNames(hashtagNames);
    }
}
