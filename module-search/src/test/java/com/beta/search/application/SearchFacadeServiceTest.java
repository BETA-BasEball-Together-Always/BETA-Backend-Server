package com.beta.search.application;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.beta.core.port.PostPort;
import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.PostInfo;
import com.beta.search.application.dto.SearchPostResult;
import com.beta.search.application.dto.SearchUserResult;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.document.UserDocument;
import com.beta.core.exception.search.SearchFailedException;
import com.beta.search.domain.service.SearchLogService;
import com.beta.search.domain.service.SearchPostService;
import com.beta.search.domain.service.SearchUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchFacadeServiceTest {

    @Mock
    private SearchLogService searchLogService;

    @Mock
    private SearchUserService searchUserService;

    @Mock
    private SearchPostService searchPostService;

    @Mock
    private PostPort postPort;

    @InjectMocks
    private SearchFacadeService searchFacadeService;

    @Test
    void searchPosts_호출시_ES검색결과와_PostPort조회결과를_합쳐서_반환한다() {
        // given
        String keyword = "테스트";
        String channel = "ALL";
        Long userId = 1L;
        SearchCursor cursor = SearchCursor.first();

        Hit<PostDocument> mockHit = createMockHit(100L, "테스트 내용입니다");
        when(searchPostService.searchInChannel(anyString(), anyString(), any(), anyInt()))
                .thenReturn(List.of(mockHit));

        PostInfo postInfo = PostInfo.builder()
                .id(100L)
                .author(AuthorInfo.builder().userId(1L).nickname("작성자").teamCode("LG").build())
                .channel("ALL")
                .imageUrls(List.of())
                .hashtags(List.of())
                .commentCount(5)
                .likeCount(10)
                .sadCount(0)
                .funCount(0)
                .hypeCount(0)
                .hasLiked(false)
                .createdAt(LocalDateTime.now())
                .build();
        when(postPort.findPostsByIds(anyList(), anyLong())).thenReturn(Map.of(100L, postInfo));

        // when
        SearchPostResult result = searchFacadeService.searchPosts(keyword, channel, userId, cursor);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().postId()).isEqualTo(100L);
        assertThat(result.posts().getFirst().author().getNickname()).isEqualTo("작성자");
    }

    @Test
    void searchPosts_호출시_PostPort에_없는_게시글은_결과에서_제외된다() {
        // given
        String keyword = "테스트";
        String channel = "ALL";
        Long userId = 1L;
        SearchCursor cursor = SearchCursor.first();

        Hit<PostDocument> mockHit1 = createMockHit(100L, "존재하는 게시글");
        Hit<PostDocument> mockHit2 = createMockHit(999L, "삭제된 게시글");
        when(searchPostService.searchInChannel(anyString(), anyString(), any(), anyInt()))
                .thenReturn(List.of(mockHit1, mockHit2));

        PostInfo postInfo = PostInfo.builder()
                .id(100L)
                .author(AuthorInfo.builder().userId(1L).nickname("작성자").teamCode("LG").build())
                .channel("ALL")
                .imageUrls(List.of())
                .hashtags(List.of())
                .commentCount(0)
                .likeCount(0)
                .sadCount(0)
                .funCount(0)
                .hypeCount(0)
                .hasLiked(false)
                .createdAt(LocalDateTime.now())
                .build();
        // 999L은 DB에 없음
        when(postPort.findPostsByIds(anyList(), anyLong())).thenReturn(Map.of(100L, postInfo));

        // when
        SearchPostResult result = searchFacadeService.searchPosts(keyword, channel, userId, cursor);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().get(0).postId()).isEqualTo(100L);
    }

    @Test
    void searchPosts_호출시_검색로그가_저장된다() {
        // given
        String keyword = "테스트";
        String channel = "ALL";
        Long userId = 1L;
        SearchCursor cursor = SearchCursor.first();

        when(searchPostService.searchInChannel(anyString(), anyString(), any(), anyInt()))
                .thenReturn(List.of());
        when(postPort.findPostsByIds(anyList(), anyLong())).thenReturn(Map.of());

        // when
        searchFacadeService.searchPosts(keyword, channel, userId, cursor);

        // then
        verify(searchLogService).save(keyword, userId, "POST");
    }

    @Test
    void searchUsers_호출시_UserDocument_목록을_SearchUserResult로_변환하여_반환한다() {
        // given
        String keyword = "닉네임";
        Long userId = 1L;
        SearchCursor cursor = SearchCursor.first();

        UserDocument userDoc = UserDocument.builder()
                .id(10L)
                .nickname("테스트닉네임")
                .bio("자기소개")
                .teamCode("LG")
                .teamNameKr("LG 트윈스")
                .build();
        when(searchUserService.search(anyString(), any(), anyInt())).thenReturn(List.of(userDoc));

        // when
        SearchUserResult result = searchFacadeService.searchUsers(keyword, userId, cursor);

        // then
        assertThat(result.users()).hasSize(1);
        assertThat(result.users().getFirst().nickname()).isEqualTo("테스트닉네임");
    }

    @Test
    void searchPosts_호출시_ES검색이_실패하면_SearchFailedException이_발생한다() {
        // given
        String keyword = "테스트";
        String channel = "ALL";
        Long userId = 1L;
        SearchCursor cursor = SearchCursor.first();

        when(searchPostService.searchInChannel(anyString(), anyString(), any(), anyInt()))
                .thenThrow(new SearchFailedException());

        // when & then
        assertThatThrownBy(() -> searchFacadeService.searchPosts(keyword, channel, userId, cursor))
                .isInstanceOf(SearchFailedException.class);

        verify(searchLogService, never()).save(anyString(), anyLong(), anyString());
    }

    @SuppressWarnings("unchecked")
    private Hit<PostDocument> createMockHit(Long postId, String content) {
        Hit<PostDocument> hit = mock(Hit.class);
        PostDocument doc = PostDocument.builder()
                .id(postId)
                .content(content)
                .channel("ALL")
                .authorNickname("작성자")
                .hashtags(List.of())
                .createdAt(LocalDateTime.now())
                .build();
        when(hit.source()).thenReturn(doc);
        when(hit.highlight()).thenReturn(Map.of("content", List.of("<em>테스트</em> 내용")));
        return hit;
    }
}
