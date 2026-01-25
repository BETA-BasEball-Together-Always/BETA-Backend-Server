package com.beta.community.domain.service;

import com.beta.community.domain.entity.Hashtag;
import com.beta.community.domain.entity.Post;
import com.beta.community.infra.repository.HashtagJpaRepository;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @InjectMocks
    private HashtagService hashtagService;

    @Mock
    private HashtagJpaRepository hashtagJpaRepository;

    @Mock
    private PostHashtagJpaRepository postHashtagJpaRepository;

    @Nested
    @DisplayName("decrementUsageCounts")
    class DecrementUsageCounts {

        @Test
        @DisplayName("해시태그 목록이 null이면 아무 작업도 하지 않음")
        void doNothing_whenHashtagNamesIsNull() {
            hashtagService.decrementUsageCounts(null);

            verifyNoInteractions(hashtagJpaRepository);
        }

        @Test
        @DisplayName("해시태그 목록이 비어있으면 아무 작업도 하지 않음")
        void doNothing_whenHashtagNamesIsEmpty() {
            hashtagService.decrementUsageCounts(Collections.emptyList());

            verifyNoInteractions(hashtagJpaRepository);
        }

        @Test
        @DisplayName("해시태그 목록이 있으면 usageCount 감소 호출")
        void callDecrement_whenHashtagNamesExist() {
            List<String> hashtagNames = List.of("야구", "축구");

            hashtagService.decrementUsageCounts(hashtagNames);

            verify(hashtagJpaRepository).decrementUsageCountByTagNames(hashtagNames);
        }
    }

    @Nested
    @DisplayName("processHashtags 엣지 케이스")
    class EdgeCase {

        @Test
        @DisplayName("해시태그 목록이 null이면 빈 리스트 반환")
        void returnEmptyList_whenHashtagNamesIsNull() {
            Post post = mock(Post.class);

            List<String> result = hashtagService.processHashtags(post, null);

            assertThat(result).isEmpty();
            verifyNoInteractions(hashtagJpaRepository);
            verifyNoInteractions(postHashtagJpaRepository);
        }

        @Test
        @DisplayName("해시태그 목록이 비어있으면 빈 리스트 반환")
        void returnEmptyList_whenHashtagNamesIsEmpty() {
            Post post = mock(Post.class);

            List<String> result = hashtagService.processHashtags(post, Collections.emptyList());

            assertThat(result).isEmpty();
            verifyNoInteractions(hashtagJpaRepository);
            verifyNoInteractions(postHashtagJpaRepository);
        }

        @Test
        @DisplayName("모든 해시태그가 null이거나 빈 문자열이면 빈 리스트 반환")
        void returnEmptyList_whenAllHashtagsAreInvalid() {
            Post post = mock(Post.class);
            List<String> hashtagNames = Arrays.asList(null, "", "  ", null);

            List<String> result = hashtagService.processHashtags(post, hashtagNames);

            assertThat(result).isEmpty();
            verifyNoInteractions(postHashtagJpaRepository);
        }

        @Test
        @DisplayName("중복된 해시태그는 한 번만 처리")
        void processDuplicateHashtagsOnce() {
            Post post = mock(Post.class);

            List<String> hashtagNames = Arrays.asList("야구", "야구", "축구", "야구");

            Hashtag hashtag1 = createHashtag(1L, "야구");
            Hashtag hashtag2 = createHashtag(2L, "축구");
            given(hashtagJpaRepository.findByTagNameIn(anyList()))
                    .willReturn(List.of(hashtag1, hashtag2));

            List<String> result = hashtagService.processHashtags(post, hashtagNames);

            assertThat(result).hasSize(2).containsExactly("야구", "축구");
            verify(hashtagJpaRepository, times(2)).insertIgnore(anyString());
        }
    }

    private Hashtag createHashtag(Long id, String tagName) {
        Hashtag hashtag = Hashtag.builder().tagName(tagName).build();
        try {
            var idField = hashtag.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(hashtag, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return hashtag;
    }
}
